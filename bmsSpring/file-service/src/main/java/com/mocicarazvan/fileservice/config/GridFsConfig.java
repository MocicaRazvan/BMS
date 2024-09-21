package com.mocicarazvan.fileservice.config;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class GridFsConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;
    @Value("${spring.data.mongodb.chunk-size}")
    private int chunkSize;

    @Value("${spring.data.mongodb.connection-pool.max-pool-size}")
    private int maxPoolSize;

    @Bean
    public MongoClient reactiveMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .applyToConnectionPoolSettings(builder -> builder.maxSize(maxPoolSize)
                        .minSize(5)
                        .maxWaitTime(200, TimeUnit.SECONDS)
                )
                .applyToSocketSettings(builder -> builder
                        .connectTimeout(60, TimeUnit.SECONDS)
                )
                .retryWrites(true)
                .retryReads(true)
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabase reactiveMongoDatabase(MongoClient mongoClient) {
        String databaseName = new ConnectionString(mongoUri).getDatabase();
        assert databaseName != null;
        return mongoClient.getDatabase(databaseName);
    }

    @Bean
    public GridFSBucket gridFSBucket(MongoDatabase reactiveMongoDatabase) {
        return GridFSBuckets.create(reactiveMongoDatabase)
                .withChunkSizeBytes(chunkSize);
    }

    @Bean
    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory(MongoClient mongoClient) {
        String databaseName = new ConnectionString(mongoUri).getDatabase();
        assert databaseName != null;
        return new SimpleReactiveMongoDatabaseFactory(mongoClient, databaseName);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory);
    }

    @Bean
    public ReactiveGridFsTemplate reactiveGridFsTemplate(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory, MappingMongoConverter mappingMongoConverter) {
        return new ReactiveGridFsTemplate(reactiveMongoDatabaseFactory, mappingMongoConverter);
    }
}
