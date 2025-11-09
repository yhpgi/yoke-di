package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.Singleton
import io.github.yhpgi.yoke.annotation.YokeComponent
import io.github.yhpgi.yoke.annotation.YokeEntryPoint

/**
 * The root DI component for the application.
 * - `@YokeEntryPoint`: Marks this as the main entry point for DI. Yoke will
 *   generate the `YokeApplication` based on this component.
 * - `@Singleton`: A scope annotation indicating that any unscoped dependencies
 *   within this component will default to singleton, and dependencies annotated
 *   with `@Singleton` will have their lifecycle tied to this component (the app's lifetime).
 * - `@YokeComponent`: Defines this interface as a DI component.
 */
@YokeEntryPoint
@Singleton
@YokeComponent
interface AppComponent
