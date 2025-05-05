package com.mocicarazvan.userservice.cache.redis.annotations;

import com.mocicarazvan.templatemodule.enums.Role;

import java.lang.annotation.*;

/**
 * The annotation provides caching functionality for reactive methods.
 * <p>
 * Arguments:
 * <ul>
 *  <li><b>key</b> - the key to be used for caching. It can be a string or a SPEL with the root this.</li>
 *  <li><b>id</b> - the id of the object to be cached. Must be set on a <b>Mono</b> return.</li>
 *  <li><b>idPath</b> - the id of the object to be cached. Must be set on a <b>Flux</b> return.</li>
 *  <li><b>saveToCache</b> - if set to false, the cache will not be saved.</li>
 *  <li><b>role</b> -  the <b>FLUX</b> cache will be saved with the role key.</li>
 *  <li><b>roleArgumentPath</b> - the path to the role for <b>FLUX</b> argument.it will overwrite the role</li>
 *  </ul>
 * <p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveRoleCache {
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

    /**
     * The role of the object to be cached. Must be set on a <b>FLUX</b> return.
     */
    Role role() default Role.ROLE_USER;

    /**
     * The path to the role for <b>FLUX</b> argument. It will overwrite the role.
     */
    String roleArgumentPath() default "";
}
