package com.mocicarazvan.ollamasearch.dtos.responses;


import io.r2dbc.spi.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.function.Function;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ItemWIthSimilarity<T> {
    private T item;
    private Double similarity;

    public <M> ItemWIthSimilarity<M> convert(Function<T, M> converter) {
        return new ItemWIthSimilarity<>(converter.apply(item), similarity);
    }

    public static <T> ItemWIthSimilarity<T> createFromRow(Row row, Function<Row, T> converter) {
        return ItemWIthSimilarity.<T>builder()
                .item(converter.apply(row))
                .similarity(row.get("similarity", Double.class))
                .build();
    }
}
