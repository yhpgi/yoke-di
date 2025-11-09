package io.github.yhpgi.yoke.sample.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.yhpgi.yoke.annotation.AndroidEntryPoint
import io.github.yhpgi.yoke.di.injectGlobal
import io.github.yhpgi.yoke.sample.domain.AnalyticsService

/**
 * A sample Android `Worker` that demonstrates non-composable dependency injection.
 * Yoke is initialized at app startup via the `YokeContentProvider`, making global
 * injection available throughout the app lifecycle.
 */
@AndroidEntryPoint
class AndroidSampleWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params) {

  override suspend fun doWork(): Result {
    /**
     * Here we use the global `injectGlobal()` DSL function to get a dependency
     * outside of a Composable context.
     */
    val analytics = injectGlobal<AnalyticsService>()
    val message = analytics.trackEvent("AndroidWorkerExecuted")
    println(message)
    return Result.success()
  }
}
