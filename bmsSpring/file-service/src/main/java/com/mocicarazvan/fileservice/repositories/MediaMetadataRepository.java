package com.mocicarazvan.fileservice.repositories;

import com.mocicarazvan.fileservice.models.MediaMetadata;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MediaMetadataRepository extends ReactiveMongoRepository<MediaMetadata, String> {
    Mono<Void> deleteAllByMediaIdIn(List<String> mediaIds);

    Mono<Void> deleteAllByMediaId(String mediaId);
}
