package com.mocicarazvan.kanbanservice.repositories;

import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

public interface KanbanColumnRepository extends ManyToOneUserRepository<KanbanColumn> {

    Flux<KanbanColumn> findAllByUserId(Long userId, Sort sort);

    @Query("""
            SELECT * FROM kanban_column
            WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<KanbanColumn> findModelByMonth(int month, int year);

}
