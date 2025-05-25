package com.mocicarazvan.postservice.repositories.impl;


import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.repositories.PostExtendedRepository;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class PostExtendedRepositoryImpl implements PostExtendedRepository {

    private final DatabaseClient databaseClient;
    private final PostMapper modelMapper;
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;
    private final OllamaQueryUtils ollamaQueryUtils;
    private final OllamaAPIService ollamaAPIService;
    private final EmbedCache embedCache;


    private static final String SELECT_ALL = "SELECT p.* FROM post p join post_embedding e on p.id = e.entity_id ";
    private static final String COUNT_ALL = "SELECT COUNT(p.id) FROM post p join post_embedding e on p.id = e.entity_id ";
    private static final String ASSOCIATIVE_ENTITY_JOIN = "JOIN post_likes pl ON p.id = pl.master_id ";

    @Override
    public Flux<Post> getPostsFiltered(String title, Boolean approved, List<String> tags, Long likedUserId,
                                       LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                       LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                       PageRequest pageRequest) {


        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {
            String query = likedUserId != null ? SELECT_ALL + ASSOCIATIVE_ENTITY_JOIN : SELECT_ALL;
            StringBuilder queryBuilder = new StringBuilder(query);

            appendWhereClause(queryBuilder, title, embeddings, approved, tags, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            repositoryUtils.addNotNullField(likedUserId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > query.length()),
                    " pl.child_id = :user_like_id");


            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, tags, queryBuilder, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            executeSpec = repositoryUtils.bindNotNullField(likedUserId, executeSpec, "user_like_id");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Flux<Post> getPostsFilteredTrainer(String title, Boolean approved, List<String> tags, Long trainerId,
                                              LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                              LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound,
                                              PageRequest pageRequest) {


        return ollamaAPIService.getEmbedding(title, embedCache).flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);


            appendWhereClause(queryBuilder, title, embeddings, approved, tags, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > SELECT_ALL.length()),
                    " user_id = :trainerId");


            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, tags, queryBuilder, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> modelMapper.fromRowToModel(row)).all();
        });
    }


    @Override
    public Mono<Long> countPostsFiltered(String title, Boolean approved, List<String> tags, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                         LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound, Long likedUserId) {
        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {
            String query = likedUserId != null ? COUNT_ALL + ASSOCIATIVE_ENTITY_JOIN : COUNT_ALL;
            StringBuilder queryBuilder = new StringBuilder(query);


            appendWhereClause(queryBuilder, title, embeddings, approved, tags, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            repositoryUtils.addNotNullField(likedUserId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > query.length()),
                    " pl.child_id = :user_like_id");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, tags, queryBuilder, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            executeSpec = repositoryUtils.bindNotNullField(likedUserId, executeSpec, "user_like_id");


            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }

    @Override
    public Mono<Long> countPostsFilteredTrainer(String title, Boolean approved, Long trainerId, List<String> tags, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {

        return ollamaAPIService.getEmbedding(title, embedCache).flatMap(embeddings -> {

            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);


            appendWhereClause(queryBuilder, title, embeddings, approved, tags, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            repositoryUtils.addNotNullField(trainerId, queryBuilder, new RepositoryUtils.MutableBoolean(queryBuilder.length() > COUNT_ALL.length()),
                    " user_id = :trainerId");

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(title, approved, tags, queryBuilder, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            executeSpec = repositoryUtils.bindNotNullField(trainerId, executeSpec, "trainerId");

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).first();
        });
    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(String title, Boolean approved, List<String> tags, StringBuilder queryBuilder, LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());


        executeSpec = repositoryUtils.bindNotNullField(approved, executeSpec, "approved");
        executeSpec = repositoryUtils.bindStringField(title, executeSpec, "title");
        executeSpec = repositoryUtils.bindCreatedAtBound(createdAtLowerBound, createdAtUpperBound, executeSpec);
        executeSpec = repositoryUtils.bindUpdatedAtBound(updatedAtLowerBound, updatedAtUpperBound, executeSpec);

        if (tags != null && !tags.isEmpty()) {
            for (int i = 0; i < tags.size(); i++) {
                executeSpec = repositoryUtils.bindNotNullField(tags.get(i), executeSpec, "tag" + i);
            }
        }

        return executeSpec;
    }


    private void appendWhereClause(StringBuilder queryBuilder, String title, String embeddings, Boolean approved, List<String> tags,
                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {

        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);

        repositoryUtils.addNotNullField(approved, queryBuilder, hasPreviousCriteria, "approved = :approved");
        repositoryUtils.addStringField(title, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR title ILIKE '%' || :title || '%' OR title % :title "));
        repositoryUtils.addCreatedAtBound("p", createdAtLowerBound, createdAtUpperBound, queryBuilder, hasPreviousCriteria);
        repositoryUtils.addUpdatedAtBound("p", updatedAtLowerBound, updatedAtUpperBound, queryBuilder, hasPreviousCriteria);


        if (tags != null && !tags.isEmpty()) {
            StringBuilder tagsBuilder = getTagsQuery(tags);
            repositoryUtils.addField(queryBuilder, hasPreviousCriteria, tagsBuilder.toString());

        }
    }

    private static StringBuilder getTagsQuery(List<String> tags) {
        StringBuilder tagsBuilder = new StringBuilder();
        tagsBuilder.append("CAST(array[ ");
        for (int i = 0; i < tags.size(); i++) {
            tagsBuilder.append(":tag").append(i);
            if (i < tags.size() - 1) {
                tagsBuilder.append(", ");
            }
        }
        tagsBuilder.append(" ] AS text[]) <@ tags ");
        return tagsBuilder;
    }
}
