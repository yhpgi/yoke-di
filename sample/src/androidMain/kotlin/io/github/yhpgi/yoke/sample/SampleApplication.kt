package io.github.yhpgi.yoke.sample

import android.app.Application
import io.github.yhpgi.yoke.annotation.AndroidEntryPoint

/**
 * The main `Application` class for the sample app on Android.
 *
 * This class is annotated with `@AndroidEntryPoint`, which is a placeholder annotation
 * from a previous version. With the `YokeContentProvider` registered in the manifest,
 * Yoke is initialized automatically, so no manual setup is required here.
 */
@AndroidEntryPoint
class SampleApplication : Application()
