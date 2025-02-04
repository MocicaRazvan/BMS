package com.mocicarazvan.orderservice.config;


import com.mocicarazvan.orderservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.orderservice.convertors.DietTypeWritingConvertor;
import com.mocicarazvan.orderservice.convertors.ObjectiveTypeReadingConvertor;
import com.mocicarazvan.orderservice.convertors.ObjectiveTypeWritingConvertor;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DbConfig extends AbstractR2dbcConfiguration {


    @Value("${spring.r2dbc.url}")
    private String url;

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(url);
    }

    @Override
    protected List<Object> getCustomConverters() {
        return Arrays.asList(
                new ObjectiveTypeReadingConvertor(),
                new ObjectiveTypeWritingConvertor(),
                new DietTypeWritingConvertor(),
                new DietTypeReadingConvertor()
        );
    }


}
