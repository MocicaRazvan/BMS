package com.mocicarazvan.rediscache.local;

import java.util.Collection;

public interface RemoveFromCache {
    void removeNotify(Collection<String> keys);

    void removeNotify(String key);

    void removeByPrefixNotify(String prefix);

    void removeByPrefixNotify(Collection<String> prefixes);

    void remove(Collection<String> keys);

    void remove(String key);

    void removeByPrefix(String prefix);

    void removeByPrefix(Collection<String> prefixes);
}
