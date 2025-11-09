package io.github.yhpgi.yoke.annotation

/**
 * Marks a class or function as part of Yoke's DSL API.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.BINARY)
annotation class YokeDsl
