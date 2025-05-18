package com.mocicarazvan.fileservice.service.impl;

import com.mocicarazvan.fileservice.config.AsyncConfig;
import com.mocicarazvan.fileservice.config.GridFsConfig;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Testcontainers
@DataMongoTest
@AutoConfigureDataMongo
@Import({
        GridFsConfig.class,
        BytesServiceImpl.class,
        AsyncConfig.class
})
@ImportAutoConfiguration({MongoReactiveDataAutoConfiguration.class})
@Execution(ExecutionMode.SAME_THREAD)
class BytesServiceImplTest {

    @Container
    static final MongoDBContainer embedded = new MongoDBContainer(DockerImageName.parse("mongo:8.0.8"));

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",
                embedded::getReplicaSetUrl);
        registry.add("spring.data.mongodb.chunk-size", () -> 5);
        registry.add("spring.data.mongodb.connection-pool.max-pool-size", () -> 10);
    }

    @Autowired
    ReactiveMongoTemplate mongoTemplate;

    @Autowired
    ReactiveGridFsTemplate gridFsTemplate;

    @Autowired
    BytesServiceImpl bytesService;

    @Autowired
    DataBufferFactory dataBufferFactory;


    private ObjectId fileId;

    @BeforeEach
    void setUp() {
        mongoTemplate.getMongoDatabase()
                .flatMapMany(db -> Flux.concat(
                        db.getCollection("fs.files").drop(),
                        db.getCollection("fs.chunks").drop()
                ))
                .blockLast();

        byte[] allBytes = new byte[15];
        for (int i = 0; i < 15; i++) {
            allBytes[i] = (byte) i;
        }

        fileId = gridFsTemplate
                .store(
                        Flux.just(dataBufferFactory.wrap(allBytes)),
                        "video.mp4"
                )
                .block();
    }

    private byte[] collect(Flux<DataBuffer> flux) {
        DataBuffer joined = DataBufferUtils.join(flux).block();
        try {
            assert joined != null;
            byte[] all = new byte[joined.readableByteCount()];
            joined.read(all);
            return all;
        } finally {
            DataBufferUtils.release(joined);
        }
    }

    private ReactiveGridFsResource loadResource() {
        GridFSFile file = gridFsTemplate
                .findOne(Query.query(
                        Criteria.where("_id").is(fileId)
                ))
                .block();
        assert file != null;
        return gridFsTemplate.getResource(file).block();
    }

    @Test
    void fullFile_returns0to14() {
        ReactiveGridFsResource res = loadResource();
        byte[] out = collect(
                bytesService.getVideoByRange(res, 0, 14)
        );
        assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14}, out);
    }

    @Test
    void crossChunk_3to7_returns3to7() {
        ReactiveGridFsResource res = loadResource();
        byte[] out = collect(
                bytesService.getVideoByRange(res, 3, 7)
        );
        assertArrayEquals(new byte[]{3, 4, 5, 6, 7}, out);
    }

    @Test
    void singleByte_middle_returnsOnly6() {
        ReactiveGridFsResource res = loadResource();
        byte[] out = collect(
                bytesService.getVideoByRange(res, 6, 6)
        );
        System.out.println(Arrays.toString(out));
        assertArrayEquals(new byte[]{6}, out);
    }

    @Test
    void pastEnd_clipToFileEnd() {
        ReactiveGridFsResource res = loadResource();
        byte[] out = collect(
                bytesService.getVideoByRange(res, 12, 20)
        );
        assertArrayEquals(new byte[]{12, 13, 14}, out);
    }

    @Test
    void completelyOutOfBounds_emptyFlux() {
        ReactiveGridFsResource res = loadResource();
        StepVerifier.create(
                bytesService.getVideoByRange(res, 20, 25)
        ).verifyComplete();
    }

    @Test
    void negativeEnd_immediateComplete() {
        ReactiveGridFsResource res = loadResource();
        StepVerifier.create(
                bytesService.getVideoByRange(res, 0, -1)
        ).verifyComplete();
    }

    @Test
    void notDivisibleByChunkSize() {
        byte[] allBytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            allBytes[i] = (byte) i;
        }
        var fileId = gridFsTemplate
                .store(
                        Flux.just(dataBufferFactory.wrap(allBytes)),
                        "video.mp4"
                )
                .block();
        assert fileId != null;
        var file = gridFsTemplate
                .findOne(Query.query(
                        Criteria.where("_id").is(fileId)
                ))
                .block();
        assert file != null;
        ReactiveGridFsResource res = gridFsTemplate.getResource(file).block();
        byte[] out = collect(
                bytesService.getVideoByRange(res, 0, 15)
        );
        assertArrayEquals(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, out);

    }

}