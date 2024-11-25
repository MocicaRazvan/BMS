package com.mocicarazvan.rediscache.annotation;


import com.mocicarazvan.rediscache.enums.BooleanEnum;

import java.lang.annotation.*;

/**
 * The annotation provides caching functionality for reactive methods.
 * <p>
 * Usage details:
 * <ul>
 *     <li>If the method returns a <b>Flux&lt;T&gt;</b>, the annotation will process and return a <b>Flux&lt;T&gt;</b>.</li>
 *     <li>If the method does not invalidate the cache, it will handle a <b>Mono&lt;T&gt;</b> and return a <b>Mono&lt;T&gt;</b>.</li>
 *     <li>If the method invalidates the cache, it will <b>EXPECT</b> a <b>Mono&lt;Pair&lt;T, Boolean&gt;&gt;</b>
 * </ul>
 * Arguments:
 * <ul>
 *  <li><b>key</b> - the key to be used for caching. It can be a string or a SPEL with the root this.</li>
 *  <li><b>id</b> - the id of the object to be cached. Must be set on a <b>Mono</b> return.</li>
 *  <li><b>idPath</b> - the id of the object to be cached. Must be set on a <b>Flux</b> return.</li>
 *  <li><b>saveToCache</b> - if set to false, the cache will not be saved.</li>
 *  <li><b>approved</b> -  the <b>FLUX</b> cache will be saved with the approved key.</li>
 *  <li><b>approvedArgumentPath</b> - the path to the approved for <b>FLUX</b> argument.it will overwrite the approved</li>
 *  <li><b>forWhom</b> - for whom is the cache. Must be set on a <b>Flux</b> return. The values are:
 *      <ol>
 *          <li>-1L means for public <b>DEFAULT</b></li>
 *          <li>0L means for admin</li>
 *          <li>trainerId means for trainer</li>
 *      </ol>
 *  </li>
 *  </ul>
 * <p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisReactiveApprovedCache {
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
     * Approved is for flux to be in the key, for one its just id
     */

    BooleanEnum approved() default BooleanEnum.NULL;

    String approvedArgumentPath() default "";

    String forWhom() default "-1";


}
