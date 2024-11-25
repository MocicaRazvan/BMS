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
 *  <li><b>id</b> - the id of the reverse index to be invalidated.</li>
 *  <li><b>masterId</b> - the id of the master object of which children to be deleted. If no masterId is specified -1 means the ones independent of a master</li>
 *  <li><b>masterPath</b> - the path to the master object of which children to be deleted. It will overwrite masterId</li>
 *  </ul>
 *  <b>At least one id, masterId or masterPath has to be present</b>
 * <p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveChildCacheEvict {
    String key() default "";

    /**
     * The id of the object to be cached. Must be set on a <b>Mono</b> return.
     */
    String id() default "";

    String masterId() default "-1";

    String masterPath() default "";
}
