package io.github.yhpgi.yoke.di

import android.app.Application
import android.content.Context

/**
 * A holder for Android-specific context and initialization logic for Yoke.
 * This object provides a way to access the application context from anywhere in the app,
 * which is crucial for many Android-specific dependencies.
 */
object AndroidYoke {
  private var applicationContext: Context? = null
  @Volatile
  private var isInitialized = false

  /**
   * Initializes AndroidYoke by capturing the application context. This should be called
   * in your `Application.onCreate()` method if you are not using the automatic
   * `YokeContentProvider` initialization. This method DOES NOT initialize the DI graph.
   *
   * @param application The main Application instance.
   */
  fun initialize(application: Application) {
    if (!isInitialized) {
      applicationContext = application.applicationContext
      isInitialized = true
    }
  }

  /**
   * Internal initialization method used by `YokeContentProvider`.
   */
  internal fun autoInitialize(context: Context) {
    if (!isInitialized) {
      applicationContext = context.applicationContext
      isInitialized = true
    }
  }

  /**
   * Retrieves the application context.
   *
   * @return The application `Context`.
   * @throws IllegalStateException if Yoke has not been initialized.
   */
  fun getApplicationContext(): Context {
    return applicationContext ?: error(
      "AndroidYoke not initialized. Either:\n" +
        "1. Call AndroidYoke.initialize(application) in your Application.onCreate(), or\n" +
        "2. Ensure the YokeContentProvider is registered in your AndroidManifest.xml (auto-initialization)."
    )
  }

  /**
   * Checks if Yoke has been initialized on Android.
   */
  fun isInitialized(): Boolean = isInitialized
}

/**
 * Provides a DSL-style way to inject dependencies using the Android `Context`.
 * This is a convenience function that delegates to [injectGlobal].
 *
 * Example:
 * ```
 * val repository = context.inject<Repository> {
 *   qualifiedBy(PremiumUser::class)
 * }
 * ```
 * @param T The type of the dependency to inject.
 * @param builder A lambda to configure the injection, e.g., by specifying a qualifier.
 * @return An instance of the requested dependency.
 */
inline fun <reified T : Any> Context.inject(
  builder: InjectBuilder<T>.() -> Unit = {}
): T = injectGlobal(builder)


/**
 * A base `Application` class that automatically initializes AndroidYoke.
 * Extend this class for a simple way to set up Yoke in your Android app.
 */
abstract class YokeApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    AndroidYoke.initialize(this)
  }
}
