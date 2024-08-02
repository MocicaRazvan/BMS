package com.mocicarazvan.recipeservice.conifg;

import com.mocicarazvan.recipeservice.convertors.DietTypeReadingConvertor;
import com.mocicarazvan.recipeservice.convertors.DietTypeWritingConvertor;
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
                new DietTypeReadingConvertor(),
                new DietTypeWritingConvertor()

        );
    }


}
