package com.mocicarazvan.templatemodule.repositories.impl;

import com.mocicarazvan.templatemodule.repositories.AssociativeEntityRepository;
import org.springframework.data.util.Pair;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.Collection;

public class AssociativeEntityRepositoryImpl implements AssociativeEntityRepository {

    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    private final SqlTemplates sqlTemplates;

    public AssociativeEntityRepositoryImpl(DatabaseClient databaseClient,
                                           TransactionalOperator transactionalOperator,
                                           String tableName) {
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
        this.sqlTemplates = new SqlTemplates(tableName);
    }

    @Override
    public Mono<Long> insertForMasterAndChildren(Long masterId, Collection<Long> childIds) {
        return Mono.defer(() -> {
            if (childIds == null || childIds.isEmpty()) {
                return Mono.error(new IllegalArgumentException("Collection must not be null or empty"));
            }
            return databaseClient.sql(sqlTemplates.insertSql)
                    .bind("masterId", masterId)
                    .bind("childIds", childIds.toArray(Long[]::new))
                    .fetch()
                    .rowsUpdated();
        });
    }

    @Override
    public Mono<Long> consensusChildrenForMaster(Long masterId,
                                                 Collection<Long> newChildIds) {

        Long[] newArr = newChildIds.toArray(Long[]::new);

        return databaseClient.sql(sqlTemplates.consensusSql.getFirst())
                .bind("masterId", masterId)
                .bind("newChildIds", newArr)
                .fetch()
                .rowsUpdated()
                // sequentially bc of table lock
                .flatMap(cnt -> databaseClient.sql(sqlTemplates.consensusSql.getSecond())
                        .bind("masterId", masterId)
                        .bind("newChildIds", newArr)
                        .fetch()
                        .rowsUpdated()
                        .map(deleted -> cnt + deleted)
                )
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Long> addChild(Long masterId, Long childId) {
        return databaseClient.sql(sqlTemplates.addChildSql)
                .bind("masterId", masterId)
                .bind("childId", childId)
                .fetch()
                .rowsUpdated();
    }

    @Override
    public Mono<Long> removeChild(Long masterId, Long childId) {
        return databaseClient.sql(sqlTemplates.removeChildSql)
                .bind("masterId", masterId)
                .bind("childId", childId)
                .fetch()
                .rowsUpdated();
    }


    private static class SqlTemplates {
        private final String insertSql;
        private final Pair<String, String> consensusSql;
        private final String addChildSql;
        private final String removeChildSql;


        private SqlTemplates(String tableName) {
            this.insertSql = buildInsertSql(tableName);
            this.consensusSql = buildConsensusSql(tableName);
            this.addChildSql = buildAddChildSql(tableName);
            this.removeChildSql = buildRemoveChildSql(tableName);
        }

        private String buildInsertSql(String table) {
            String tpl = """
                    INSERT INTO %1$s (master_id, child_id, multiplicity)
                    SELECT :masterId, x, cnt
                    FROM (
                      SELECT x, COUNT(*) AS cnt
                      FROM UNNEST(CAST(:childIds AS BIGINT[])) AS x
                      GROUP BY x
                    ) AS batch
                    ON CONFLICT (master_id, child_id) DO UPDATE
                    SET multiplicity = %1$s.multiplicity + EXCLUDED.multiplicity
                    """;
            return String.format(tpl, table);
        }

        private Pair<String, String> buildConsensusSql(String table) {
            String upsertTpl = """
                    INSERT INTO %1$s (master_id, child_id, multiplicity)
                    SELECT :masterId, x, cnt
                    FROM (
                      SELECT x, COUNT(*) AS cnt
                      FROM UNNEST(CAST(:newChildIds AS BIGINT[])) AS x
                      GROUP BY x
                    ) AS batch
                    ON CONFLICT (master_id, child_id) DO UPDATE
                    SET multiplicity = EXCLUDED.multiplicity
                    """;

            String pruneTpl = """
                    DELETE FROM %1$s
                    WHERE master_id = :masterId
                      AND child_id NOT IN (
                        SELECT DISTINCT x
                        FROM UNNEST(CAST(:newChildIds AS BIGINT[])) AS x
                      )
                    """;

            return Pair.of(
                    String.format(upsertTpl, table),
                    String.format(pruneTpl, table)
            );
        }

        private String buildAddChildSql(String table) {
            String tpl = """
                    INSERT INTO %1$s (master_id, child_id, multiplicity)
                    VALUES (:masterId, :childId, 1)
                    ON CONFLICT (master_id, child_id) DO UPDATE
                    SET multiplicity = %1$s.multiplicity + 1
                    """;
            return String.format(tpl, table);
        }

        private String buildRemoveChildSql(String table) {
            String tpl = """
                    WITH decremented AS (
                      UPDATE %1$s
                      SET multiplicity = multiplicity - 1
                      WHERE master_id = :masterId
                        AND child_id   = :childId
                        AND multiplicity > 1
                      RETURNING 1
                    )
                    DELETE FROM %1$s
                    WHERE master_id = :masterId
                      AND child_id   = :childId
                      AND NOT EXISTS (SELECT 1 FROM decremented)
                    """;
            return String.format(tpl, table);
        }

    }
}
