package com.mocicarazvan.userservice.repositories.impl;

import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import com.mocicarazvan.userservice.cache.embedding.EmbedUserCache;
import com.mocicarazvan.userservice.mappers.UserMapper;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.ExtendedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ExtendedUserRepositoryImpl implements ExtendedUserRepository {
    private final UserMapper userMapper;

    private final String SELECT_ALL = "SELECT u.* FROM user_custom u join user_embedding e on u.id = e.entity_id ";
    private final String COUNT_ALL = "SELECT COUNT(u.id) FROM user_custom u join user_embedding e on u.id = e.entity_id ";
    private final PageableUtilsCustom pageableUtils;
    private final RepositoryUtils repositoryUtils;
    private final OllamaQueryUtils ollamaQueryUtils;
    private final OllamaAPIService ollamaAPIService;
    private final EmbedUserCache embedUserCache;
    private final DatabaseClient databaseClient;

    @Override
    public Flux<UserCustom> getUsersFiltered(PageRequest pageRequest, String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified) {
        List<String> roleList = List.copyOf(roles).stream().map(Role::name).toList();
        List<String> providerList = List.copyOf(providers).stream().map(AuthProvider::name).toList();
        Mono<String> embeddingsMono = getEmbeddingsMono(email);

        return embeddingsMono.flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, email, embeddings, roleList, providerList, emailVerified);


            if (repositoryUtils.isNotNullOrEmpty(embeddings)) {
                queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest,
                        ollamaQueryUtils.addOrder(
                                embeddings
                        )
                ));
            } else {
                queryBuilder.append(pageableUtils.createPageRequestQuery(pageRequest));
            }


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(queryBuilder, email, roleList, providerList, emailVerified);

            return executeSpec.map((row, metadata) -> userMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Mono<Long> countUsersFiltered(String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified) {
        List<String> roleList = List.copyOf(roles).stream().map(Role::name).toList();
        List<String> providerList = List.copyOf(providers).stream().map(AuthProvider::name).toList();
        Mono<String> embeddingsMono = getEmbeddingsMono(email);

        return embeddingsMono.flatMap(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);
            appendWhereClause(queryBuilder, email, embeddings, roleList, providerList, emailVerified);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(queryBuilder, email, roleList, providerList, emailVerified);

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).one();
        });
    }

    private Mono<String> getEmbeddingsMono(String email) {
        return repositoryUtils.isNotNullOrEmpty(email)
                ? ollamaAPIService.generateEmbeddingMono(email, embedUserCache).map(ollamaQueryUtils::getEmbeddingsAsString)
                : Mono.just("");
    }

    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(StringBuilder queryBuilder, String email, List<String> roleList, List<String> providerList, Boolean emailVerified) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());
        executeSpec = repositoryUtils.bindStringField(email, executeSpec, "email");
        executeSpec = repositoryUtils.bindArrayField(roleList, executeSpec, "roles", String.class);
        executeSpec = repositoryUtils.bindArrayField(providerList, executeSpec, "providers", String.class);
        executeSpec = repositoryUtils.bindNotNullField(emailVerified, executeSpec, "emailVerified");
        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String email, String embeddings, List<String> roleList, List<String> providerList, Boolean emailVerified) {
        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);
        repositoryUtils.addStringField(email, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR email ILIKE '%' || :email || '%' OR similarity(email, :email ) > 0.35  "));
        repositoryUtils.addListField(roleList, queryBuilder, hasPreviousCriteria, "role = ANY(:roles) ");
        repositoryUtils.addListField(providerList, queryBuilder, hasPreviousCriteria, "provider = ANY(:providers) ");
        repositoryUtils.addNotNullField(emailVerified, queryBuilder, hasPreviousCriteria, "is_email_verified = :emailVerified ");

    }
}
