package com.mocicarazvan.rediscache.impl;

import com.mocicarazvan.rediscache.local.LocalReactiveCache;
import com.mocicarazvan.rediscache.local.ReverseKeysLocalCache;
import com.mocicarazvan.rediscache.local.SynchronizeLocalRemove;


public class SynchronizeLocalRemoveTestImpl extends SynchronizeLocalRemove {
    public SynchronizeLocalRemoveTestImpl(LocalReactiveCache localReactiveCache, ReverseKeysLocalCache reverseKeysLocalCache) {
        super(localReactiveCache, reverseKeysLocalCache);
    }
}
