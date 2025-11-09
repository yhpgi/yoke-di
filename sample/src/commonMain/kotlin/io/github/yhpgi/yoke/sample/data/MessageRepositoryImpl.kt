package io.github.yhpgi.yoke.sample.data

import io.github.yhpgi.yoke.annotation.Binds
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent
import io.github.yhpgi.yoke.sample.domain.MessageRepository

/**
 * The concrete implementation of [MessageRepository].
 * This is a simple example of using `@Binds` to bind an
 * implementation to its interface.
 */
@Injectable
@Binds(to = MessageRepository::class)
@ContributesTo(AppComponent::class)
class MessageRepositoryImpl : MessageRepository {
  override fun getMessage(): String = "This message comes from a real implementation, bound to an interface."
}
