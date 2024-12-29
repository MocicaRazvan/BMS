package com.mocicarazvan.kanbanservice.config;

import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.templatemodule.services.RabbitMqUpdateDeleteService;
import com.mocicarazvan.templatemodule.services.impl.RabbitMqUpdateDeleteNoOpServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public RabbitMqUpdateDeleteService<KanbanColumn> kanbanColumnRabbitMqUpdateDeleteService() {
        return new RabbitMqUpdateDeleteNoOpServiceImpl<>();
    }

    @Bean
    public RabbitMqUpdateDeleteService<KanbanTask> kanbanTaskRabbitMqUpdateDeleteService() {
        return new RabbitMqUpdateDeleteNoOpServiceImpl<>();
    }
}
