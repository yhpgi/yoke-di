# Konsep Inti: Qualifier

Terkadang Anda perlu menyediakan beberapa implementasi berbeda untuk interface yang sama. Misalnya, Anda mungkin memiliki `UserRepository` untuk pengguna tamu dan satu lagi untuk pengguna premium.

```kotlin
interface UserRepository {
    fun getUsername(): String
}

class GuestUserRepositoryImpl : UserRepository { /* ... */ }
class PremiumUserRepositoryImpl : UserRepository { /* ... */ }
```

Jika Anda mencoba menginjeksi `UserRepository`, Yoke tidak akan tahu implementasi mana yang harus disediakan. Di sinilah **Qualifier** berperan. Qualifier adalah anotasi yang Anda gunakan untuk memberi label pada binding, sehingga Yoke dapat membedakannya.

## `@YokeQualifier`

Langkah pertama adalah membuat anotasi qualifier Anda sendiri. Buat anotasi baru dan tandai dengan `@YokeQualifier`.

```kotlin
import io.github.yhpgi.yoke.annotation.YokeQualifier

@YokeQualifier
@Retention(AnnotationRetention.SOURCE)
annotation class GuestUser

@YokeQualifier
@Retention(AnnotationRetention.SOURCE)
annotation class PremiumUser
```

## `@QualifiedBy`

Selanjutnya, gunakan anotasi qualifier yang baru Anda buat untuk memberi label pada implementasi atau fungsi `@Provides` Anda menggunakan `@QualifiedBy`.

### Memberi Label pada Implementasi `@Injectable`

```kotlin
@Injectable
@UserScope
@ContributesTo(UserComponent::class)
@QualifiedBy(GuestUser::class) // Memberi label ini sebagai GuestUser
@Binds(to = UserRepository::class)
class GuestUserRepositoryImpl : UserRepository {
    override fun getUsername(): String = "Guest User"
}

@Injectable
@UserScope
@ContributesTo(UserComponent::class)
@QualifiedBy(PremiumUser::class) // Memberi label ini sebagai PremiumUser
@Binds(to = UserRepository::class)
class PremiumUserRepositoryImpl : UserRepository {
    override fun getUsername(): String = "Premium Member"
}
```

### Memberi Label pada Fungsi `@Provides`

Anda juga dapat menggunakan `@QualifiedBy` pada fungsi `@Provides`.

```kotlin
@Module
@ContributesTo(AppComponent::class)
object ConfigModule {
    @Provides
    @QualifiedBy(ApiUrl::class)
    fun provideApiUrl(): String = "https://api.example.com"

    @Provides
    @QualifiedBy(ApiKey::class)
    fun provideApiKey(): String = "ABC-123-XYZ"
}
```

## Menginjeksi dengan Qualifier

Untuk menginjeksi dependensi yang telah diberi qualifier, gunakan parameter `qualifiedBy` di dalam DSL `inject` atau `injectViewModel`.

### Injeksi di Composable

```kotlin
@Composable
fun UserProfile() {
    val guestRepo = inject<UserRepository> {
        qualifiedBy(GuestUser::class)
    }

    val premiumRepo = inject<UserRepository> {
        qualifiedBy(PremiumUser::class)
    }

    Text("Guest: ${guestRepo.getUsername()}")
    Text("Premium: ${premiumRepo.getUsername()}")
}
```

### Injeksi di Konstruktor

Anda juga dapat meminta dependensi ber-qualifier di konstruktor kelas `@Injectable` lain dengan memberi anotasi pada parameter.

```kotlin
@Injectable
@ContributesTo(UserComponent::class)
class UserViewModel(
    @QualifiedBy(PremiumUser::class)
    private val userRepository: UserRepository
) : ViewModel() {
    // ...
}
```
