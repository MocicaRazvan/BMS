package com.mocicarazvan.postservice.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountProcedureCreation {
    private final DatabaseClient client;


    private Mono<Boolean> tableExists() {
        return client
                .sql("SELECT EXISTS (SELECT FROM pg_tables WHERE tablename = :table)")
                .bind("table", "post_view_count")
                .map(row -> row.get(0, Boolean.class))
                .one()
                .defaultIfEmpty(false);
    }

    @PostConstruct
    public void init() {
        Flux.interval(Duration.ofSeconds(5))
                .flatMap(_ -> tableExists())
                .filter(exists -> exists)
                .take(1)
                .flatMap(_ -> createProcedure())
                .flatMap(_ -> callProcedure())
                .subscribe(
                        _ -> log.info("Procedure created and called for post_view_count"),
                        error -> log.error("Error creating procedure", error)
                );
    }

    public Mono<Boolean> createProcedure() {
        return client.sql("""
                            CREATE OR REPLACE PROCEDURE ensure_table_partitions(months_ahead INTEGER,
                                                                                target_table TEXT,
                                                                                date_column TEXT,
                                                                                reference_column TEXT
                            )
                                LANGUAGE plpgsql
                            AS $$
                            DECLARE
                                current_month_start DATE := date_trunc('month', CURRENT_DATE);
                                partition_start DATE;
                                partition_end DATE;
                                partition_name TEXT;
                                i INTEGER := 0;
                                default_exists BOOLEAN;
                            BEGIN
                                WHILE i < months_ahead LOOP
                                        partition_start := current_month_start + (i || ' months')::INTERVAL;
                                        partition_end := partition_start + INTERVAL '1 month';
                                        partition_name := format('%I_%s', target_table, to_char(partition_start, 'YYYY_MM'));
                        
                                        IF NOT EXISTS (
                                            SELECT FROM pg_tables WHERE tablename = partition_name
                                        ) THEN
                                            EXECUTE format(
                                                    'CREATE TABLE IF NOT EXISTS %I PARTITION OF %I
                                                     FOR VALUES FROM (%L) TO (%L);',
                                                    partition_name,
                                                    target_table,
                                                    partition_start,
                                                    partition_end
                                                    );
                                        END IF;
                        
                                        RAISE NOTICE 'Created partition %', partition_name;
                        
                                        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_date ON %I (%I);',
                                                       target_table,
                                                       partition_name,
                                                       date_column
                                                );
                        
                                        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%I_%I ON %I (%I);',
                                                       target_table,
                                                       reference_column,
                                                       partition_name,
                                                       reference_column
                                                );
                        
                                        RAISE NOTICE 'Created index for partition %', partition_name;
                        
                                        i := i + 1;
                                    END LOOP;
                        
                                SELECT EXISTS (
                                    SELECT FROM pg_inherits
                                                    JOIN pg_class ON pg_inherits.inhrelid = pg_class.oid
                                    WHERE pg_inherits.inhparent =  format('%I', target_table)::regclass
                                      AND relname =  format('%I_default', target_table)
                                ) INTO default_exists;
                        
                                IF NOT default_exists THEN
                                    EXECUTE '
                                        CREATE TABLE IF NOT EXISTS  ' || target_table || '_default PARTITION OF ' || target_table || ' DEFAULT;
                                    ';
                                    RAISE NOTICE 'Created default partition for %', target_table;
                        
                                    EXECUTE ' CREATE INDEX IF NOT EXISTS idx_' || target_table || '_default_date ON ' || target_table || '_default (' || date_column || ');';
                                    EXECUTE ' CREATE INDEX IF NOT EXISTS idx_' || target_table || '_default_' || reference_column || ' ON ' || target_table || '_default (' || reference_column || ');';
                                    RAISE NOTICE 'Created index for default partition %', target_table;
                                END IF;
                            END;
                            $$;
                        """
                )
                .then().thenReturn(true);
    }

    private Mono<Boolean> callProcedure() {
        return client.sql("""
                CALL ensure_table_partitions(12, 'post_view_count', 'access_date', 'post_id');
                """).then().thenReturn(true);
    }
}
