package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.YokeQualifier

/**
 * A qualifier annotation to identify the `UserRepository` implementation
 * for guest users.
 * Qualifiers are used when there is more than one way to provide a dependency
 * of the same type.
 *
 * @see YokeQualifier
 */
@YokeQualifier
@Retention(AnnotationRetention.SOURCE)
annotation class GuestUser

/**
 * A qualifier annotation to identify the `UserRepository` implementation
 * for premium users.
 *
 * @see YokeQualifier
 */
@YokeQualifier
@Retention(AnnotationRetention.SOURCE)
annotation class PremiumUser
