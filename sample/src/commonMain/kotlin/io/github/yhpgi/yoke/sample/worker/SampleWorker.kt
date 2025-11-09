package io.github.yhpgi.yoke.sample.worker

import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.di.injectGlobal
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.AnalyticsService

/**
 * A sample class demonstrating injection outside of a Composable context using the DSL.
 */
class SampleWorker {
  /**
   * Performs some work and uses an injected dependency.
   * This uses the global `injectGlobal` DSL function.
   *
   * @return The result of the work.
   */
  fun doWork(): String {
    val analytics = injectGlobal<AnalyticsService>()
    return analytics.trackEvent("WorkerExecuted")
  }
}

/**
 * A manager class that is itself injectable and uses the non-composable [SampleWorker].
 */
@Injectable
@ContributesTo(AppComponent::class)
class WorkerManager {
  /**
   * Creates a worker instance and executes its task.
   *
   * @return A string with the result from the worker.
   */
  fun executeWorker(): String {
    val worker = SampleWorker()
    val result = worker.doWork()
    return "WorkerManager: $result"
  }
}
