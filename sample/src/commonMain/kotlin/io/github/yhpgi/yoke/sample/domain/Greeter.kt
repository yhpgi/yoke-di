package io.github.yhpgi.yoke.sample.domain

import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent

/**
 * A simple example class to demonstrate basic constructor injection.
 * - `@Injectable`: Tells Yoke how to create an instance of this class by calling its constructor.
 * - `@ContributesTo`: Tells Yoke that this class is available within the `AppComponent`.
 */
@Injectable
@ContributesTo(AppComponent::class)
class Greeter {
  /**
   * Generates a greeting message.
   * @return A greeting string.
   */
  fun greet(): String = "Hello from an @Injectable class!"
}
