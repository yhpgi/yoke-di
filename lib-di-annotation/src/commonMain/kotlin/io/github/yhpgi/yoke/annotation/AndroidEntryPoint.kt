package io.github.yhpgi.yoke.annotation

/**
 * Marks an Android component class to be a target for Yoke's initialization.
 *
 * When applied to an `Application` class, it helps in setting up the global DI container
 * automatically without needing to extend `YokeApplication`.
 * This is a marker annotation and is used to signal auto-initialization hooks.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AndroidEntryPoint
