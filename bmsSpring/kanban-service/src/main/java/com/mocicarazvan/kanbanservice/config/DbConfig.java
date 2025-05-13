package com.mocicarazvan.kanbanservice.config;


import com.mocicarazvan.kanbanservice.convertors.KanbanTaskTypeReadingConvertor;
import com.mocicarazvan.kanbanservice.convertors.KanbanTaskTypeWritingConvertor;
import com.mocicarazvan.kanbanservice.models.KanbanColumn;
import com.mocicarazvan.kanbanservice.models.KanbanTask;
import com.mocicarazvan.templatemodule.dbCallbacks.IdGeneratedBeforeSaveCallback;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.mapping.event.BeforeSaveCallback;

@Configuration
public class DbConfig {

    @Bean
    public R2dbcCustomConversions getCustomConverters(ConnectionFactory connectionFactory) {
        return R2dbcCustomConversions.of(DialectResolver.getDialect(connectionFactory),
                new KanbanTaskTypeReadingConvertor(),
                new KanbanTaskTypeWritingConvertor()
        );
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<KanbanColumn> kanbanColumnBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public BeforeSaveCallback<KanbanTask> kanbanTaskBeforeSaveCallback() {
        return new IdGeneratedBeforeSaveCallback<>();
    }


}
