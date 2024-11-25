package com.mocicarazvan.rediscache.annotation;

import java.lang.annotation.*;

/**
 * The annotation provides caching functionality for reactive methods.
 * <p>
 * Usage details:
 * <ul>
 *     <li>If the method invalidates the cache, it will <b>EXPECT</b> a <b>Mono&lt;Pair&lt;T, Boolean&gt;&gt;</b>
 *     where the Boolean is the old value of the approved for the given object</li>
 * </ul>
 * Arguments:
 * <ul>
 *  <li><b>key</b> - the key to be used for caching. It can be a string or a SPEL with the root this.</li>
 *  <li><b>id</b> - the id of the reverse index to be invalidated.</li>
 *   <li><b>forWhomPath</b> - the path to the forWhom that was set in the cache for a list.(-1L public, 0L admin, else trainerId)</li>
 *  </ul>
 * <p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveApprovedCacheEvict {
    String key() default "";

    /**
     * The id of the object to be cached. Must be set on a <b>Mono</b> return.
     */
    String id() default "";

    String forWhomPath() default "";
}
