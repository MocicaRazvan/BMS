package com.mocicarazvan.templatemodule.cache.keys;

import java.util.List;

public class ChildFilterKey extends FilerKeyTypeWithExtra<Long, Long> {

    public ChildFilterKey() {
        super();
    }

    public ChildFilterKey(List<Long> ids, String key, Long defaultMap) {
        super(ids, key, defaultMap);
    }

    public ChildFilterKey(String key, Long defaultMap) {
        super(key, defaultMap);
    }

    public ChildFilterKey(Long defaultMap) {
        super(defaultMap);
    }

    public ChildFilterKey(String key, KeyRouteType routeType, Long defaultMap) {
        super(key, routeType, defaultMap);
    }

    public ChildFilterKey(List<Long> ids, String key, KeyRouteType routeType, Long defaultMap) {
        super(ids, key, routeType, defaultMap);
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
        return "ChildFilterKey(ids=" + this.getIds() + ", key=" + this.getKey() + ", extra=" + this.getExtra() + ")";
    }

    @Override
    public Long mapToExtra(Long map) {
        return map;
    }


}
