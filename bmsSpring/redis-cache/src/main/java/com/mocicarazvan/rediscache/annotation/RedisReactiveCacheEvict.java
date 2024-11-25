package com.mocicarazvan.rediscache.annotation;

import java.lang.annotation.*;

/**
 * The annotation provides caching functionality for reactive methods.
 * <p>
 * Usage details:
 * <ul>
 *     <li>If the method returns a <b>Mono&lt;T&gt;</b>, the annotation will process and return a <b>Mono&lt;T&gt;</b>.</li>
 * </ul>
 * Arguments:
 * <ul>
 *  <li><b>key</b> - the key to be used for caching. It can be a string or a SPEL with the root this.</li>
 *  <li><b>id</b> - the id of the reverse index to be invalidated. Must be set on a <b>Mono</b> return.</li>
 *  </ul>
 * <p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveCacheEvict {
    String key() default "";

    /**
     * The id of the object to be cached. Must be set on a <b>Mono</b> return.
     */
    String id() default "";
}
