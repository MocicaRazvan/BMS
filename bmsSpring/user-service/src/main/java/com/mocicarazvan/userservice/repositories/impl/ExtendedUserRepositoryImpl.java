package com.mocicarazvan.userservice.repositories.impl;

import com.mocicarazvan.ollamasearch.cache.EmbedCache;
import com.mocicarazvan.ollamasearch.services.OllamaAPIService;
import com.mocicarazvan.ollamasearch.utils.OllamaQueryUtils;
import com.mocicarazvan.templatemodule.enums.AuthProvider;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import com.mocicarazvan.templatemodule.utils.RepositoryUtils;
import com.mocicarazvan.userservice.mappers.UserMapper;
import com.mocicarazvan.userservice.models.UserCustom;
import com.mocicarazvan.userservice.repositories.ExtendedUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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
    private final EmbedCache embedCache;
    private final DatabaseClient databaseClient;

    @Override
    public Flux<UserCustom> getUsersFiltered(PageRequest pageRequest, String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified,
                                             LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                             LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        List<String> roleList = List.copyOf(roles).stream().map(Role::name).toList();
        List<String> providerList = List.copyOf(providers).stream().map(AuthProvider::name).toList();

        return ollamaAPIService.getEmbedding(email, embedCache).flatMapMany(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(SELECT_ALL);
            appendWhereClause(queryBuilder, email, embeddings, roleList, providerList, emailVerified, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);


            pageableUtils.appendPageRequestQueryCallbackIfFieldIsNotEmpty(queryBuilder, pageRequest, embeddings, ollamaQueryUtils::addOrder);


            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(queryBuilder, email, roleList, providerList, emailVerified, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            return executeSpec.map((row, metadata) -> userMapper.fromRowToModel(row)).all();
        });
    }

    @Override
    public Mono<Long> countUsersFiltered(String email, Set<Role> roles, Set<AuthProvider> providers, Boolean emailVerified,
                                         LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                         LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound) {
        List<String> roleList = List.copyOf(roles).stream().map(Role::name).toList();
        List<String> providerList = List.copyOf(providers).stream().map(AuthProvider::name).toList();


        return ollamaAPIService.getEmbedding(email, embedCache).flatMap(embeddings -> {
            StringBuilder queryBuilder = new StringBuilder(COUNT_ALL);
            appendWhereClause(queryBuilder, email, embeddings, roleList, providerList, emailVerified, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            DatabaseClient.GenericExecuteSpec executeSpec = getGenericExecuteSpec(queryBuilder, email, roleList, providerList, emailVerified, createdAtLowerBound, createdAtUpperBound, updatedAtLowerBound, updatedAtUpperBound);

            return executeSpec.map((row, metadata) -> row.get(0, Long.class)).one();
        });
    }


    private DatabaseClient.GenericExecuteSpec getGenericExecuteSpec(StringBuilder queryBuilder, String email, List<String> roleList, List<String> providerList, Boolean emailVerified,
                                                                    LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                                                    LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(queryBuilder.toString());
        executeSpec = repositoryUtils.bindStringField(email, executeSpec, "email");
        executeSpec = repositoryUtils.bindArrayField(roleList, executeSpec, "roles", String.class);
        executeSpec = repositoryUtils.bindArrayField(providerList, executeSpec, "providers", String.class);
        executeSpec = repositoryUtils.bindNotNullField(emailVerified, executeSpec, "emailVerified");
        executeSpec = repositoryUtils.bindCreatedAtBound(createdAtLowerBound, createdAtUpperBound, executeSpec);
        executeSpec = repositoryUtils.bindUpdatedAtBound(updatedAtLowerBound, updatedAtUpperBound, executeSpec);
        return executeSpec;
    }

    private void appendWhereClause(StringBuilder queryBuilder, String email, String embeddings, List<String> roleList, List<String> providerList, Boolean emailVerified,
                                   LocalDate createdAtLowerBound, LocalDate createdAtUpperBound,
                                   LocalDate updatedAtLowerBound, LocalDate updatedAtUpperBound
    ) {
        RepositoryUtils.MutableBoolean hasPreviousCriteria = new RepositoryUtils.MutableBoolean(false);
        repositoryUtils.addStringField(email, queryBuilder, hasPreviousCriteria, ollamaQueryUtils.addThresholdFilter(embeddings, " OR email ILIKE '%' || :email || '%' OR  email % :email "));
        repositoryUtils.addListField(roleList, queryBuilder, hasPreviousCriteria, "role = ANY(:roles) ");
        repositoryUtils.addListField(providerList, queryBuilder, hasPreviousCriteria, "provider = ANY(:providers) ");
        repositoryUtils.addNotNullField(emailVerified, queryBuilder, hasPreviousCriteria, "is_email_verified = :emailVerified ");
        repositoryUtils.addCreatedAtBound("u", createdAtLowerBound, createdAtUpperBound, queryBuilder, hasPreviousCriteria);
        repositoryUtils.addUpdatedAtBound("u", updatedAtLowerBound, updatedAtUpperBound, queryBuilder, hasPreviousCriteria);
    }
}
