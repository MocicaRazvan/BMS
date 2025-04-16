package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.enums.MediaType;
import com.mocicarazvan.fileservice.service.BytesService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class BytesServiceImpl implements BytesService {

    private final Scheduler thumbnailScheduler;

    @Value("${spring.custom.video.limit-rate:16}")
    private int videoLimitRate;

    @Value("${spring.custom.thumblinator.limit-rate:16}")
    private int thumblinatorLimitRate;

    public BytesServiceImpl(@Qualifier("threadPoolTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.thumbnailScheduler = Schedulers.fromExecutor(threadPoolTaskScheduler.getScheduledExecutor());

    }


    @Override
    public Flux<DataBuffer> getVideoByRange(ReactiveGridFsResource file, AtomicLong rangeStart, AtomicLong rangeEnd) {
        Flux<DataBuffer> downloadStream;
        int chunkSize = file.getOptions().getChunkSize();
        Flux<DataBuffer> dataBufferFlux = chunkSize > 0 ? file.getDownloadStream(chunkSize) : file.getDownloadStream();
        downloadStream = dataBufferFlux
                .subscribeOn(Schedulers.boundedElastic())
                .limitRate(videoLimitRate, videoLimitRate / 2)
                .handle((dataBuffer, sink) -> {
                    try {
                        if (rangeStart.get() < 0 || rangeEnd.get() < 0) {
                            DataBufferUtils.release(dataBuffer);
                            return;
                        }
                        int dataBufferSize = dataBuffer.readableByteCount();
                        // skip data until the start of the range
                        if (rangeStart.get() >= dataBufferSize) {
                            rangeStart.addAndGet(-dataBufferSize);
                            rangeEnd.addAndGet(-dataBufferSize);
                            DataBufferUtils.release(dataBuffer);
                            return;
                        }

                        // slice data buffer to fit the start of the range
                        if (rangeStart.get() > 0) {
                            int sliceStart = (int) rangeStart.get();
                            int sliceLength = dataBufferSize - sliceStart;
                            dataBuffer = dataBuffer.slice(sliceStart, sliceLength);
                            rangeStart.set(0);
                        }

                        // slice data buffer to fit the end of the range
                        if (rangeEnd.get() < dataBufferSize) {
                            //range start its 0 anyway
                            int sliceEnd = (int) (rangeEnd.get() - rangeStart.get() + 1);
                            if (sliceEnd < dataBufferSize) {
                                dataBuffer = dataBuffer.slice(0, sliceEnd);
                            }
                            rangeEnd.set(-1);
                        } else {
                            rangeEnd.addAndGet(-dataBufferSize);
                        }

                        sink.next(dataBuffer);

                        if (rangeEnd.get() < 0) {
                            sink.complete();
                        }
                    } catch (Exception e) {
                        log.error("Error processing video: {}", e.getMessage(), e);
                        DataBufferUtils.release(dataBuffer);
                        sink.error(e);
                    }
                });
        return downloadStream;
    }


    @Override
    public Flux<DataBuffer> convertWithThumblinator(Integer width, Integer height, Double quality, Flux<DataBuffer> downloadStream, MediaType mediaType, ServerHttpResponse response) {
        return DataBufferUtils.join(downloadStream)
                .publishOn(Schedulers.boundedElastic())
                .flatMapMany(dataBuffer -> {
                    try (InputStream inputStream = dataBuffer.asInputStream(true)) {
                        DataBufferUtils.release(dataBuffer);

                        boolean isKnownFormat = mediaType == MediaType.PNG || mediaType == MediaType.JPEG || mediaType == MediaType.JPG;


                        ImageReader reader = null;
                        ImageInputStream imageInputStream = null;
                        String mediaFormatName = null;
                        try {
                            if (isKnownFormat) {
                                mediaFormatName = mediaType.getValue().toLowerCase();
                                Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(mediaFormatName);
                                if (!imageReaders.hasNext()) {
                                    return getImageFallback(response, inputStream);
                                }
                                reader = imageReaders.next();
                                imageInputStream = ImageIO.createImageInputStream(inputStream);
                            } else {

                                imageInputStream = ImageIO.createImageInputStream(inputStream);
                                Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

                                if (!imageReaders.hasNext()) {
                                    return getImageFallback(response, inputStream);
                                }

                                reader = imageReaders.next();
                            }

                            reader.setInput(imageInputStream);
                            BufferedImage image = reader.read(0);

                            if (image == null) {
                                log.error("Could not read image, redirecting to fallback");
                                return getImageFallback(response, inputStream);
                            }

                            String formatName = mediaFormatName != null ? mediaFormatName : reader.getFormatName();
                            if (mediaType == MediaType.ALL) {
                                response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/" + formatName.toLowerCase());
                            }
                            return Mono.fromCallable(() -> processWithThumblinator(width, height, quality, response, image, formatName))
                                    .subscribeOn(thumbnailScheduler);
                        } catch (IOException e) {
                            log.error("Error processing image: {}", e.getMessage(), e);
                            return Flux.error(new IOException("Failed to process the image"));
                        } finally {
                            if (reader != null) {
                                reader.dispose();
                            }
                            if (imageInputStream != null) {
                                imageInputStream.close();
                            }
                        }


                    } catch (Exception e) {
                        log.error("Error processing image: {}", e.getMessage(), e);
                        DataBufferUtils.release(dataBuffer);
                        return Flux.error(new IOException("Failed to process the image"));
                    }
                })
                .limitRate(thumblinatorLimitRate, thumblinatorLimitRate / 2);
    }

    private DataBuffer processWithThumblinator(Integer width, Integer height, Double quality, ServerHttpResponse response, BufferedImage image, String formatName) throws IOException {
        DataBuffer dataBuffer = response.bufferFactory().allocateBuffer(getInitialThumblinatorBufferSize(width, height, image));
        try (OutputStream outputStream = dataBuffer.asOutputStream()) {
            configureThumblinator(width, height, quality, image, formatName, outputStream);
            return dataBuffer;
        } catch (Exception e) {
            DataBufferUtils.release(dataBuffer);
            throw e;
        }
    }

    private int getInitialThumblinatorBufferSize(Integer width, Integer height, BufferedImage image) {
        int outputWidth = (width != null && width > 0) ? width : image.getWidth();
        int outputHeight = (height != null && height > 0) ? height : image.getHeight();
        return (int) (outputWidth * outputHeight * 4 * 1.2);
    }


    private void configureThumblinator(Integer width, Integer height, Double quality, BufferedImage image, String formatName, OutputStream outputStream) throws IOException {
        Thumbnails.Builder<BufferedImage> thumbnailBuilder = Thumbnails.of(image);
//        log.info("Image format: {}", formatName);

        if (formatName != null) {
            thumbnailBuilder.outputFormat(formatName);
        }

        if (width != null && height != null) {
            thumbnailBuilder.size(width, height);
            thumbnailBuilder.crop(Positions.CENTER);
        } else if (width != null) {
            if (width > 0) {
                thumbnailBuilder.width(width);
            } else {
                thumbnailBuilder.scale(1.0);
            }
        } else if (height != null) {
            if (height > 0) {
                thumbnailBuilder.height(height);
            } else {
                thumbnailBuilder.scale(1.0);
            }
        } else {
            thumbnailBuilder.scale(1.0);
        }

        if (quality != null && quality > 0) {
            double finalQuality = quality <= 100 ? quality : 100.0;
            thumbnailBuilder.outputQuality(finalQuality / 100.0f);
        }

        thumbnailBuilder.toOutputStream(outputStream);
    }


    @Override
    public Mono<DataBuffer> getImageFallback(ServerHttpResponse response, InputStream imageInputStream) {
        log.error("Falling back to default image processing");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int bytesRead;


            while ((bytesRead = imageInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] imageBytes = outputStream.toByteArray();

            return Mono.just(response.bufferFactory().wrap(ByteBuffer.wrap(imageBytes)));
        } catch (IOException e) {
            log.error("Error reading from ImageInputStream", e);
            return Mono.error(e);
        }
    }


}
