package com.mocicarazvan.kanbanservice.repositories;

import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.templatemodule.repositories.ManyToOneUserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

public interface KanbanTaskRepository extends ManyToOneUserRepository<KanbanTask> {
    Flux<KanbanTask> findAllByColumnId(Long columnId, Sort sort);

    @Query("""
            SELECT * FROM kanban_task
             WHERE created_at >= make_timestamp(:year, :month, 1, 0, 0, 0)
            AND created_at < make_timestamp(:year, :month, 1, 0, 0, 0) + INTERVAL '1 month'
            ORDER BY created_at DESC
            """)
    Flux<KanbanTask> findModelByMonth(int month, int year);
}
