package com.mocicarazvan.templatemodule.utils;

import java.util.function.Function;

public interface Transformable<T extends Transformable<T>> {

    default <R> R map(Function<T, R> mapper) {
        return mapper.apply((T) this);
    }
}
