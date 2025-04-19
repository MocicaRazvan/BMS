package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.enums.MediaType;
import com.mocicarazvan.fileservice.service.BytesService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

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

@Service
@Slf4j
public class BytesServiceImpl implements BytesService {

    private final Scheduler thumbnailScheduler;
    private final ReactiveMongoTemplate reactiveMongoTemplate;
    private final DataBufferFactory dataBufferFactory;
    @Value("${spring.custom.video.limit-rate:16}")
    private int videoLimitRate;

    @Value("${spring.custom.thumblinator.limit-rate:16}")
    private int thumblinatorLimitRate;

    public BytesServiceImpl(@Qualifier("threadPoolTaskScheduler") ThreadPoolTaskScheduler threadPoolTaskScheduler, ReactiveMongoTemplate reactiveMongoTemplate,
                            @Qualifier("dataBufferFactory") DataBufferFactory dataBufferFactory) {
        this.thumbnailScheduler = Schedulers.fromExecutor(threadPoolTaskScheduler.getScheduledExecutor());


        this.reactiveMongoTemplate = reactiveMongoTemplate;
        this.dataBufferFactory = dataBufferFactory;
    }

    @Override
    public Flux<DataBuffer> getVideoByRange(ReactiveGridFsResource file,
                                            long start,
                                            long end) {
        if (start > end || start < 0) {
            return Flux.empty();
        }
        int chunkSize = file.getOptions().getChunkSize();
        int startChunk = (int) (start / chunkSize);
        int endChunk = (int) (end / chunkSize);
        int headOffset = (int) (start % chunkSize);
        int tailOffset = (int) (end % chunkSize);
        // only the chunks that are in the range
        Query q = Query.query(Criteria
                        .where("files_id").is(file.getFileId())
                        .and("n").gte(startChunk).lte(endChunk))
                .with(Sort.by("n"));

        return reactiveMongoTemplate
                .find(q, Document.class, "fs.chunks")
                .limitRate(videoLimitRate, videoLimitRate / 2)
                .map(doc -> {
                    int chunkIdx = doc.getInteger("n");
                    Binary bin = doc.get("data", Binary.class);
                    DataBuffer buf = dataBufferFactory.wrap(bin.getData());
                    return Tuples.of(chunkIdx, buf);
                })
                .map(tuple -> {
                    int n = tuple.getT1();
                    DataBuffer buf = tuple.getT2();

                    // single chunk
                    if (startChunk == endChunk) {
                        int length = tailOffset - headOffset + 1;
                        buf.split(headOffset);                         // drop [0..headOffset)
                        return buf.split(length);                       // buf now starts at headOffset
                    }
                    // 1st chunk
                    if (n == startChunk) {
                        buf.split(headOffset);                         // drop [0..headOffset)
                        return buf;                                    // buf now starts at headOffset
                    }
                    // last chunk
                    if (n == endChunk) {
                        return buf.split(tailOffset + 1);
                    }
                    // middle chunk
                    return buf;
                });
    }


    @Override
    public Flux<DataBuffer> convertWithThumblinator(Integer width, Integer height, Double quality, Flux<DataBuffer> downloadStream, MediaType mediaType, ServerHttpResponse response) {
        return DataBufferUtils.join(downloadStream)
                .publishOn(Schedulers.boundedElastic())
                .flatMapMany(dataBuffer -> {
                    try (InputStream inputStream = dataBuffer.asInputStream(true)) {

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
