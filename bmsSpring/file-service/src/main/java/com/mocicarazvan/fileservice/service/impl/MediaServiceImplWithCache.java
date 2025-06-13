package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.dtos.GridIdsDto;
import com.mocicarazvan.fileservice.enums.CustomMediaType;
import com.mocicarazvan.fileservice.repositories.ExtendedMediaRepository;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import com.mocicarazvan.fileservice.repositories.MediaMetadataRepository;
import com.mocicarazvan.fileservice.repositories.MediaRepository;
import com.mocicarazvan.fileservice.service.BytesService;
import com.mocicarazvan.fileservice.utils.CacheHeaderUtils;
import com.mocicarazvan.fileservice.websocket.ProgressWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;

//@Service
@Slf4j
public class MediaServiceImplWithCache extends MediaServiceImpl {

    private final ImageRedisRepository imageRedisRepository;

    public MediaServiceImplWithCache(ReactiveGridFsTemplate gridFsTemplate, MediaRepository mediaRepository,
                                     MediaMetadataRepository mediaMetadataRepository, ProgressWebSocketHandler progressWebSocketHandler,
                                     BytesService bytesService, ExtendedMediaRepository extendedMediaRepository, ImageRedisRepository imageRedisRepository) {
        super(gridFsTemplate, mediaRepository, mediaMetadataRepository, progressWebSocketHandler, bytesService, extendedMediaRepository);
        this.imageRedisRepository = imageRedisRepository;
    }

    @Override
    public Mono<ServerHttpResponse> getResponseForFile(String gridId, Integer width, Integer height, Double quality, ServerWebExchange exchange) {
        return imageRedisRepository.shouldCheckCache(
                ImageRedisRepository.ImageRedisRepositoryImageCharacteristics.builder()
                        .width(width)
                        .height(height)
                        .quality(quality)
                        .build()
        ) ?
                Mono.defer(() ->
                                isWebpOutputEnabled(exchange.getRequest()).flatMap(webpOutputEnabled ->
                                        imageRedisRepository.getImage(gridId, ImageRedisRepository.ImageRedisRepositoryImageCharacteristics.builder()
                                                .width(width)
                                                .height(height)
                                                .quality(quality)
                                                .isWebpOutputEnabled(webpOutputEnabled)
                                                .build()).flatMap(
                                                model -> {
                                                    byte[] cachedImage = model.getImageData();
                                                    String attch = model.getImageCharacteristics().getIsWebpOutputEnabled() ? ".webp" : model.getAttachment();
                                                    long timestamp = model.getTimestamp();


                                                    ServerHttpResponse response = exchange.getResponse();


                                                    String etag = CacheHeaderUtils.buildETag(gridId, width, height, quality, webpOutputEnabled, timestamp);

                                                    String clientETag = exchange.getRequest().getHeaders().getFirst(HttpHeaders.IF_NONE_MATCH);
                                                    if (CacheHeaderUtils.etagEquals(etag, clientETag)) {
                                                        response.setStatusCode(HttpStatus.NOT_MODIFIED);
                                                        return Mono.just(response);
                                                    }
                                                    response.getHeaders().setCacheControl(CacheHeaderUtils.IMAGE_CACHE_CONTROL);
                                                    response.getHeaders().setETag(etag);
                                                    response.getHeaders().setLastModified(timestamp);

                                                    String mediaType;
                                                    if (model.getImageCharacteristics().getIsWebpOutputEnabled()) {
                                                        mediaType = "webp";
                                                        response.getHeaders().setContentDisposition(
                                                                ContentDisposition.inline()
                                                                        .filename(gridId + ".webp")
                                                                        .build()
                                                        );
                                                    } else {
                                                        mediaType = CustomMediaType.fromValue(attch).getContentTypeValueMedia();
                                                        response.getHeaders().setContentDisposition(
                                                                ContentDisposition.inline()
                                                                        .filename(gridId + attch)
                                                                        .build()
                                                        );

                                                    }

                                                    response.getHeaders().set(HttpHeaders.CONTENT_TYPE, "image/" + mediaType);
                                                    response.getHeaders().setContentLength(cachedImage.length);
                                                    return response.writeWith(Mono.just(response.bufferFactory().wrap(cachedImage)))
                                                            .thenReturn(response)
                                                            .onErrorResume(e -> {
                                                                response.setStatusCode(HttpStatus.FOUND);
                                                                response.getHeaders().setLocation(URI.create("/files/download/" + gridId));
                                                                return response.setComplete()
                                                                        .thenReturn(response);
                                                            });
                                                }
                                        )))
                        .switchIfEmpty(Mono.defer(() -> super.getResponseForFile(gridId, width, height, quality, exchange))) :
                Mono.defer(() -> super.getResponseForFile(gridId, width, height, quality, exchange));
    }

    @Override
    protected Flux<DataBuffer> makeImageDownloadStream(String gridId, Integer width, Integer height, Double quality, Boolean webpOutputEnabled, Flux<DataBuffer> downloadStream, CustomMediaType customMediaType, ServerHttpResponse response, String fileAttch) {
        return bytesService.convertWithThumblinator(width, height, quality, downloadStream, customMediaType, webpOutputEnabled, response
                )
                .flatMap(dataBuffer -> {
                    response.getHeaders().setContentLength(dataBuffer.readableByteCount());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(dataBuffer.readableByteCount());
                    dataBuffer.toByteBuffer(byteBuffer);
                    return imageRedisRepository.saveImage(gridId,
                                    ImageRedisRepository.ImageRedisRepositoryImageCharacteristics.builder()
                                            .width(width)
                                            .height(height)
                                            .quality(quality)
                                            .isWebpOutputEnabled(webpOutputEnabled)
                                            .build()
                                    , byteBuffer.array(), fileAttch)
                            .thenReturn(dataBuffer);
                });
    }

    @Override
    public Mono<Void> markFilesToBeDeleted(GridIdsDto ids) {
        return super.markFilesToBeDeleted(ids)
                .then(imageRedisRepository.deleteAllImagesByGridIds(ids.getGridFsIds()));
    }
}
