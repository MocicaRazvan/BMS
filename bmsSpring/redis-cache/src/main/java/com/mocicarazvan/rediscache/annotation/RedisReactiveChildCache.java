package com.mocicarazvan.rediscache.annotation;

import java.lang.annotation.*;

/**
 * The annotation provides caching functionality for reactive methods.
 * <p>
 * Usage details:
 * <ul>
 *     <li>If the method returns a <b>Flux&lt;T&gt;</b>, the annotation will process and return a <b>Flux&lt;T&gt;</b>.</li>
 *     <li>If the method does not invalidate the cache, it will handle a <b>Mono&lt;T&gt;</b> and return a <b>Mono&lt;T&gt;</b>.</li>
 * </ul>
 * Arguments:
 * <ul>
 *  <li><b>key</b> - the key to be used for caching. It can be a string or a SPEL with the root this.</li>
 *  <li><b>id</b> - the id of the object to be cached. Must be set on a <b>Mono</b> return.</li>
 *  <li><b>idPath</b> - the id of the object to be cached. Must be set on a <b>Flux</b> return.</li>
 *  <li><b>saveToCache</b> - if set to false, the cache will not be saved.</li>
 *  <li><b>masterId</b> - the id of the master object to be cached . If no masterId is specified -1 means the ones for all masters</li>
 *  </ul>
 * <p>
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveChildCache {
    String key() default "";

    /**
     * The id of the object to be cached. Must be set on a <b>Mono</b> return.
     */
    String id() default "";

    /**
     * The id of the object to be cached. Must be set on a <b>Flux</b> return.
     */
    String idPath() default "";


    boolean saveToCache() default true;

    String masterId() default "-1";

}
