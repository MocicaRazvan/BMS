package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.service.BytesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class BytesServiceImpl implements BytesService {

    public Flux<DataBuffer> getVideoByRange(ReactiveGridFsResource file, long[] rangeStart, long[] rangeEnd) {
        Flux<DataBuffer> downloadStream;
        downloadStream = file.getDownloadStream()
                .flatMap(dataBuffer -> {
                    int dataBufferSize = dataBuffer.readableByteCount();
                    // skip data until the start of the range
                    if (rangeStart[0] >= dataBufferSize) {
                        rangeStart[0] -= dataBufferSize;
                        rangeEnd[0] -= dataBufferSize;
                        DataBufferUtils.release(dataBuffer);
                        return Mono.empty();
                    }

                    // slice data buffer to fit the start of the range
                    if (rangeStart[0] > 0) {
                        int sliceStart = (int) rangeStart[0];
                        int sliceLength = dataBufferSize - sliceStart;
                        dataBuffer = dataBuffer.slice(sliceStart, sliceLength);
                        rangeStart[0] = 0;
                    }

                    // slice data buffer to fit the end of the range
                    if (rangeEnd[0] < dataBufferSize) {
                        int sliceEnd = (int) (rangeEnd[0] - rangeStart[0] + 1);
                        if (sliceEnd < dataBuffer.readableByteCount()) {
                            dataBuffer = dataBuffer.slice(0, sliceEnd);
                        }
                        rangeEnd[0] = 0;
                    } else {
                        rangeEnd[0] -= dataBufferSize;
                    }

                    return Mono.just(dataBuffer);
                });
        return downloadStream;
    }


    public Flux<DataBuffer> convertWithThumblinator(Integer width, Integer height, Double quality, Flux<DataBuffer> downloadStream, ServerHttpResponse response) {
        return DataBufferUtils.join(downloadStream)
                .publishOn(Schedulers.boundedElastic())
                .flatMapMany(dataBuffer -> {
                    try {
                        InputStream inputStream = dataBuffer.asInputStream();
                        DataBufferUtils.release(dataBuffer);

                        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
                        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);


                        if (!imageReaders.hasNext()) {
                            return getImageFallback(response, inputStream);
                        }

                        ImageReader reader = imageReaders.next();

                        reader.setInput(imageInputStream);
                        BufferedImage image = reader.read(0);

                        if (image == null) {
                            log.error("Could not read image, redirecting to fallback");
                            return getImageFallback(response, inputStream);
                        }


                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        configureThumblinator(width, height, quality, image, reader, outputStream);
                        DataBuffer buffer = response.bufferFactory().wrap(outputStream.toByteArray());
                        return Mono.just(buffer);

                    } catch (Exception e) {
                        log.error("Error processing image: {}", e.getMessage(), e);
                        DataBufferUtils.release(dataBuffer);
                        return Flux.error(new IOException("Failed to process the image"));
                    }
                });
    }

    private void configureThumblinator(Integer width, Integer height, Double quality, BufferedImage image, ImageReader reader, ByteArrayOutputStream outputStream) throws IOException {
        Thumbnails.Builder<BufferedImage> thumbnailBuilder = Thumbnails.of(image);
        String formatName = reader.getFormatName();
        log.info("Image format: {}", formatName);

        if (formatName != null) {
            thumbnailBuilder.outputFormat(formatName);
        }

        if (width != null && height != null) {
            thumbnailBuilder.size(width, height);
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


    private Thumbnails.Builder<BufferedImage> configureThumbnailBuilder(BufferedImage image, String formatName, Integer width, Integer height, Double quality) {
        Thumbnails.Builder<BufferedImage> thumbnailBuilder = Thumbnails.of(image);

        if (formatName != null) {
            thumbnailBuilder.outputFormat(formatName);
        }

        if (width != null && height != null) {
            thumbnailBuilder.size(width, height);
        } else if (width != null && width > 0) {
            thumbnailBuilder.width(width);
        } else if (height != null && height > 0) {
            thumbnailBuilder.height(height);
        } else {
            thumbnailBuilder.scale(1.0);
        }

        if (quality != null && quality > 0) {
            double finalQuality = Math.min(quality, 100.0);
            thumbnailBuilder.outputQuality(finalQuality / 100.0f);
        }

        return thumbnailBuilder;
    }

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

//    public Mono<DataBuffer> getImageFallback(ServerHttpResponse response, ByteArrayInputStream byteArrayInputStream) {
//
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//
//            while ((bytesRead = byteArrayInputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
//
//            byte[] imageBytes = outputStream.toByteArray();
//
//            return Mono.just(response.bufferFactory().wrap(ByteBuffer.wrap(imageBytes));
//        } catch (IOException e) {
//            log.error("Error reading from ByteArrayInputStream", e);
//            return Mono.error(e);
//        }
//    }


}
