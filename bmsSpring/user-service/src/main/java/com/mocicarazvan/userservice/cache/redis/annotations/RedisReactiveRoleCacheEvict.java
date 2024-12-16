package com.mocicarazvan.userservice.cache.redis.annotations;

import com.mocicarazvan.templatemodule.enums.Role;

import java.lang.annotation.*;

/**
 * The annotation provides caching functionality for reactive methods.
 * <p>
 * Usage details:
 * <ul>
 *     <li>If the method invalidates the cache with <b>useOldRole</b>, it will <b>EXPECT</b> a <b>Mono&lt;Pair&lt;T, ROLE_ANN&gt;&gt;</b>
 *     where the ROLE_ANN is the old value of the approved for the given object</li>
 * </ul>
 * Arguments:
 * <ul>
 *  <li><b>key</b> - the key to be used for caching. It can be a string or a SPEL with the root this.</li>
 *  <li><b>id</b> - the id of the reverse index to be invalidated</li>
 *  <li><b>oldRole</b> - the old role of the object to invalidate</li>
 *  <li><b>newRole</b> - the new role of the object to invalidate</li>
 *  <li><b>oldRolePath</b> - the path to the old role in the object</li>
 *  </ul>
 * <p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveRoleCacheEvict {
    String key() default "";

    /**
     * The id of the object to be cached. Must be set on a <b>Mono</b> return.
     */
    String id() default "";

    Role oldRole() default Role.ROLE_USER;

    Role newRole() default Role.ROLE_USER;

    String oldRolePath() default "";
}
