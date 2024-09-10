package com.mocicarazvan.templatemodule.cache.keys;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Supplier;


@Setter
@Getter
public abstract class FilerKeyTypeWithExtra<E, M> extends FilterKeyType {
    protected E extra;

    public FilerKeyTypeWithExtra() {
        super();
    }

    public FilerKeyTypeWithExtra(M defaultMap) {
        super();
        this.extra = mapToExtra(defaultMap);
    }

    public FilerKeyTypeWithExtra(String key, M defaultMap) {
        super(key);
        this.extra = mapToExtra(defaultMap);
    }

    public FilerKeyTypeWithExtra(List<Long> ids, String key, M defaultMap) {
        super(ids, key);
        this.extra = mapToExtra(defaultMap);
    }

    public FilerKeyTypeWithExtra(String key, FilterKeyType.KeyRouteType routeType, M defaultMap) {
        super(key, routeType);
        this.extra = mapToExtra(defaultMap);
    }

    public FilerKeyTypeWithExtra(List<Long> ids, String key, FilterKeyType.KeyRouteType routeType, M defaultMap) {
        super(ids, key, routeType);
        this.extra = mapToExtra(defaultMap);
    }


    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return "FilerKeyTypeWithExtra(ids=" + this.getIds() + ", key=" + this.getKey() + ", extra=" + this.getExtra() + ")";
    }

    public void setExtra(M map) {
        this.extra = mapToExtra(map);
    }

    public void setActualExtra(E extra) {
        this.extra = extra;
    }

    public abstract E mapToExtra(M map);

    public static <M, E> E mapToExtra(Supplier<? extends FilerKeyTypeWithExtra<E, M>> supplier, M map) {
        return supplier.get().mapToExtra(map);
    }


}
