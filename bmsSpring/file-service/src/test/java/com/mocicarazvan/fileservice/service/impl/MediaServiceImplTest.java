package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.dtos.GridIdsDto;
import com.mocicarazvan.fileservice.dtos.ToBeDeletedCounts;
import com.mocicarazvan.fileservice.models.Media;
import com.mocicarazvan.fileservice.repositories.ExtendedMediaRepository;
import com.mocicarazvan.fileservice.repositories.ImageRedisRepository;
import com.mocicarazvan.fileservice.repositories.MediaMetadataRepository;
import com.mocicarazvan.fileservice.repositories.MediaRepository;
import com.mocicarazvan.fileservice.service.BytesService;
import com.mocicarazvan.fileservice.websocket.ProgressWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static java.util.List.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    ReactiveGridFsTemplate gridFsTemplate;

    @Mock
    MediaRepository mediaRepository;

    @Mock
    MediaMetadataRepository mediaMetadataRepository;

    @Mock
    ExtendedMediaRepository extendedMediaRepository;

    @Mock
    ImageRedisRepository imageRedisRepository;

    @Mock
    BytesService bytesService;

    @Mock
    ProgressWebSocketHandler progressWebSocketHandler;

    @InjectMocks
    MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mediaService, "imagesUrl", "http://localhost:8080/images/");
        ReflectionTestUtils.setField(mediaService, "videosUrl", "http://localhost:8080/videos/");
        ReflectionTestUtils.setField(mediaService, "batchSize", 2);
    }

    @Test
    void deleteFile_existingGridFsId_completesSuccessfully() {
        String gridId = new ObjectId().toHexString();
        Media media = Media.builder().id("1").gridFsId(gridId).build();

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsId(gridId)).thenReturn(Flux.just(media));
        when(mediaMetadataRepository.deleteAllByMediaId(media.getId())).thenReturn(Mono.empty());
        when(mediaRepository.deleteAllByGridFsId(gridId)).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFile(gridId))
                .verifyComplete();

    }

    @Test
    void deleteFiles_withMixedValidAndInvalidIds_deletesOnlyValid() {
        String validId = new ObjectId().toHexString();
        String invalidId = "notAnObjectId";
        Media media1 = Media.builder().id("1").gridFsId(validId).build();

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsIdIn(of(validId, invalidId))).thenReturn(Flux.just(media1));
        when(mediaMetadataRepository.deleteAllByMediaIdIn(of(media1.getId()))).thenReturn(Mono.empty());
        when(mediaRepository.deleteAllByGridFsIdIn(of(validId, invalidId))).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFiles(of(validId, invalidId)))
                .verifyComplete();

    }

    @Test
    void countToBeDeleted_returnsExpectedCount() {
        ToBeDeletedCounts counts = new ToBeDeletedCounts(5L, 1L);
        when(extendedMediaRepository.countAllByToBeDeletedIsTrue()).thenReturn(Mono.just(counts));

        StepVerifier.create(mediaService.countToBeDeleted())
                .expectNext(counts)
                .verifyComplete();
    }

    @Test
    void deleteFileWithCacheInvalidate_marksToBeDeletedAndClearsCache() {
        List<String> ids = of(new ObjectId().toHexString(), new ObjectId().toHexString());
        when(extendedMediaRepository.markToBeDeletedByGridFsIds(ids)).thenReturn(Mono.empty());
        when(imageRedisRepository.deleteAllImagesByGridIds(ids)).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFileWithCacheInvalidate(new GridIdsDto(ids)))
                .verifyComplete();

    }

    @Test
    void deleteFile_noMediaFound_completesSuccessfully() {
        String gridId = new ObjectId().toHexString();

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsId(gridId)).thenReturn(Flux.empty());
        when(mediaRepository.deleteAllByGridFsId(gridId)).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFile(gridId))
                .verifyComplete();

        verify(gridFsTemplate).delete(argThat(q ->
                q.getQueryObject().containsValue(new ObjectId(gridId))
        ));
        verify(mediaRepository).deleteAllByGridFsId(gridId);
        verify(mediaMetadataRepository, never()).deleteAllByMediaId(any());
    }

    @Test
    void deleteFile_gridFsDeletionError_propagatesError() {
        String gridId = new ObjectId().toHexString();
        RuntimeException ex = new RuntimeException("failure");

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.error(ex));

        StepVerifier.create(mediaService.deleteFile(gridId))
                .expectErrorMessage("failure")
                .verify();
    }

    @Test
    void deleteFiles_allValidIds_deletesAllAndCompletes() {
        String id1 = new ObjectId().toHexString();
        String id2 = new ObjectId().toHexString();
        Media m1 = Media.builder().id("1").gridFsId(id1).build();
        Media m2 = Media.builder().id("2").gridFsId(id2).build();

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsIdIn(of(id1, id2))).thenReturn(Flux.just(m1, m2));
        when(mediaMetadataRepository.deleteAllByMediaIdIn(of(m1.getId(), m2.getId()))).thenReturn(Mono.empty());
        when(mediaRepository.deleteAllByGridFsIdIn(of(id1, id2))).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFiles(of(id1, id2)))
                .verifyComplete();


    }

    @Test
    void deleteFiles_allInvalidIds_completesSuccessfully() {
        String invalidId = "notAnObjectId";

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsIdIn(of(invalidId))).thenReturn(Flux.empty());
        when(mediaMetadataRepository.deleteAllByMediaIdIn(of())).thenReturn(Mono.empty());
        when(mediaRepository.deleteAllByGridFsIdIn(of(invalidId))).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFiles(of(invalidId)))
                .verifyComplete();


    }

    @Test
    void deleteFiles_emptyList_completesAndDeletesNothing() {
        List<String> empty = of();

        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsIdIn(empty)).thenReturn(Flux.empty());
        when(mediaMetadataRepository.deleteAllByMediaIdIn(empty)).thenReturn(Mono.empty());
        when(mediaRepository.deleteAllByGridFsIdIn(empty)).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFiles(empty))
                .verifyComplete();

    }

    @Test
    void countToBeDeleted_zero_returnsZeroCount() {
        ToBeDeletedCounts zero = new ToBeDeletedCounts(0L, 0L);
        when(extendedMediaRepository.countAllByToBeDeletedIsTrue()).thenReturn(Mono.just(zero));

        StepVerifier.create(mediaService.countToBeDeleted())
                .expectNext(zero)
                .verifyComplete();
    }

    @Test
    void deleteFileWithCacheInvalidate_noIds_completesWithoutError() {
        List<String> empty = of();

        when(extendedMediaRepository.markToBeDeletedByGridFsIds(empty)).thenReturn(Mono.empty());
        when(imageRedisRepository.deleteAllImagesByGridIds(empty)).thenReturn(Mono.empty());

        StepVerifier.create(mediaService.deleteFileWithCacheInvalidate(new GridIdsDto(empty)))
                .verifyComplete();

    }

    @Test
    void hardDeleteFiles_noItems_emitsNothing() {
        when(mediaRepository.findAllByToBeDeletedIsTrue()).thenReturn(Flux.empty());

        StepVerifier.create(mediaService.hardDeleteFiles())
                .verifyComplete();

        verify(mediaRepository).findAllByToBeDeletedIsTrue();
        verifyNoInteractions(gridFsTemplate, mediaMetadataRepository, imageRedisRepository, extendedMediaRepository);
    }

    @Test
    void hardDeleteFiles_singleBatch_deletesAndEmitsCount() {
        String id1 = new ObjectId().toHexString();
        String id2 = new ObjectId().toHexString();
        Media m1 = Media.builder().id("1").gridFsId(id1).build();
        Media m2 = Media.builder().id("2").gridFsId(id2).build();
        ToBeDeletedCounts counts = new ToBeDeletedCounts(2L, 0L);

        when(mediaRepository.findAllByToBeDeletedIsTrue()).thenReturn(Flux.just(m1, m2));
        when(gridFsTemplate.delete(any(Query.class))).thenReturn(Mono.empty());
        when(mediaRepository.findAllByGridFsIdIn(of(id1, id2))).thenReturn(Flux.just(m1, m2));
        when(mediaMetadataRepository.deleteAllByMediaIdIn(of(m1.getId(), m2.getId()))).thenReturn(Mono.empty());
        when(mediaRepository.deleteAllByGridFsIdIn(of(id1, id2))).thenReturn(Mono.empty());
        when(extendedMediaRepository.countAllByToBeDeletedIsTrue()).thenReturn(Mono.just(counts));

        StepVerifier.create(mediaService.hardDeleteFiles())
                .expectNext(counts)
                .verifyComplete();

    }
}