package io.github.yhpgi.yoke.di

import kotlin.concurrent.Volatile

/**
 * A global holder for the Yoke dependency injection container and resolver.
 * This object enables non-composable access to the DI graph via [injectGlobal].
 * It should be initialized once at application startup, which is handled automatically
 * by the generated `YokeApplication` composable.
 */
object YokeGlobal {
  /**
   * The global [YokeContext] instance. Available after `YokeApplication` is composed.
   */
  @Volatile
  var context: YokeContext? = null

  /**
   * The global [YokeResolver] instance. Available after `YokeApplication` is composed.
   */
  @Volatile
  var resolver: YokeResolver? = null
}
