package com.mocicarazvan.fileservice.repositories.impl;

import com.mocicarazvan.fileservice.dtos.ToBeDeletedCounts;
import com.mocicarazvan.fileservice.models.Media;
import com.mocicarazvan.fileservice.repositories.ExtendedMediaRepository;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Repository
@RequiredArgsConstructor
public class ExtendedMediaRepositoryImpl implements ExtendedMediaRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Long> markToBeDeletedByGridFsIds(List<String> gridFsIds) {
        return mongoTemplate.updateMulti(
                new Query(Criteria.where("gridFsId").in(gridFsIds)),
                new Update().set("toBeDeleted", true),
                Media.class
        ).map(UpdateResult::getModifiedCount);
    }

    @Override
    public Mono<ToBeDeletedCounts> countAllByToBeDeletedIsTrue() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("toBeDeleted").is(true)),
                Aggregation.addFields()
                        .addField("gridFsIdObjectId")
                        .withValue(ConvertOperators.ToObjectId.toObjectId("$gridFsId"))
                        .build(),
                Aggregation.facet(
                                Aggregation.count().as("mediaCount")
                        ).as("mediaStats")
                        .and(
                                Aggregation.lookup("fs.files",
                                        "gridFsIdObjectId"
                                        , "_id", "fileMatches"),
                                Aggregation.unwind("fileMatches"),
                                Aggregation.count().as("fileCount")
                        ).as("fileStats")
                ,
                Aggregation.project()
                        .and(
                                ConditionalOperators.IfNull.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("$mediaStats.mediaCount").elementAt(0)
                                ).then(0))
                        .as("mediaCount")

                        .and(ConditionalOperators.IfNull.ifNull(
                                        ArrayOperators.ArrayElemAt.arrayOf("$fileStats.fileCount").elementAt(0))
                                .then(0))
                        .as("fileCount")
        );
        return
                mongoTemplate.aggregate(aggregation, Media.class, ToBeDeletedCounts.class)
                        .next();
    }

}
