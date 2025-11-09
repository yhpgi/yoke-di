# Konsep Inti: Scoping

*Scoping* memungkinkan Anda untuk mengontrol siklus hidup dari sebuah dependensi di dalam komponennya. Secara default, setiap kali Anda menginjeksi sebuah dependensi, Yoke akan membuat instance baru. Namun, dengan *scoping*, Anda bisa membuat Yoke menyimpan dan menggunakan kembali instance yang sama selama komponen tersebut aktif.

## `@Singleton`

`@Singleton` adalah *scope* bawaan yang paling umum. Dependensi yang ditandai `@Singleton` akan dibuat sekali per komponen dan instance yang sama akan digunakan kembali untuk semua injeksi berikutnya selama komponen tersebut hidup.

Untuk komponen root (`AppComponent`), ini berarti instance tersebut akan hidup selama aplikasi berjalan.

```kotlin
import io.github.yhpgi.yoke.annotation.Singleton

// AuthRepositoryImpl akan menjadi singleton di dalam AppComponent
@Singleton
@Injectable
@ContributesTo(AppComponent::class)
class AuthRepositoryImpl : AuthRepository {
    // ...
}
```

Anda juga bisa menggunakan `@Singleton` pada fungsi `@Provides`:

```kotlin
@Module
@ContributesTo(AppComponent::class)
object DataModule {
    @Provides
    @Singleton // AnalyticsService akan menjadi singleton
    fun provideAnalyticsService(): AnalyticsService {
        return AnalyticsService("https://api.example.com")
    }
}
```

## Scope Kustom dengan `@Scope`

Anda dapat mendefinisikan anotasi *scope* Anda sendiri untuk mengikat siklus hidup dependensi ke subkomponen kustom. Ini sangat berguna untuk *scope* seperti sesi pengguna.

**1. Definisikan Anotasi Scope**
Buat anotasi baru dan tandai dengan `@Scope`.

```kotlin
import io.github.yhpgi.yoke.annotation.Scope

@Scope
@Retention(AnnotationRetention.SOURCE)
annotation class UserScope
```

**2. Terapkan Scope pada Subkomponen**
Gunakan anotasi *scope* kustom Anda pada `interface` subkomponen.

```kotlin
@UserScope // Mengikat siklus hidup komponen ini ke UserScope
@YokeSubcomponent
@ContributesTo(AppComponent::class)
interface UserComponent : DIContainer
```

**3. Terapkan Scope pada Dependensi**
Sekarang, tandai dependensi yang ingin Anda batasi siklus hidupnya ke `UserComponent` dengan `@UserScope`.

```kotlin
@UserScope // UserRepository ini akan hidup selama UserComponent aktif
@Injectable
@ContributesTo(UserComponent::class)
class PremiumUserRepositoryImpl : UserRepository {
    // ...
}
```
Instance `PremiumUserRepositoryImpl` akan dibuat sekali saat `UserComponent` dibuat dan akan digunakan kembali untuk semua injeksi di dalam *scope* tersebut. Ketika `UserComponent` dihancurkan (misalnya, saat pengguna logout), instance ini juga akan dihancurkan.

## Manajemen Scope dengan DSL `scope<T>{}`

Yoke menyediakan DSL `scope<T>{}` yang elegan untuk mengelola siklus hidup subkomponen secara deklaratif di dalam UI Compose Anda.

```kotlin
@Composable
fun MainContent() {
    val authRepository = inject<AuthRepository>()
    val isLoggedIn by authRepository.isLoggedIn.collectAsState()

    // Mengelola siklus hidup UserComponent
    scope<UserComponent> {
        // Kondisi untuk mengaktifkan scope
        active { isLoggedIn }

        // Konten yang akan ditampilkan saat scope aktif
        whenActive {
            // Anda bisa inject dependensi dari UserComponent di sini
            val userViewModel = injectViewModel<UserViewModel>()
            UserProfileScreen(userViewModel)
        }

        // Konten yang akan ditampilkan saat scope tidak aktif
        whenInactive {
            LoginScreen()
        }

        // Callback opsional saat scope diaktifkan/dinonaktifkan
        onActivate { println("UserComponent DIBUAT") }
        onDeactivate { println("UserComponent DIHANCURKAN") }
    }
}
```
DSL `scope<T>{}` secara otomatis:
- Membuat `UserComponent` ketika `isLoggedIn` menjadi `true`.
- Menghancurkan `UserComponent` dan semua dependensi `@UserScope` di dalamnya ketika `isLoggedIn` menjadi `false`.
- Memastikan dependensi yang tepat tersedia di dalam blok `whenActive`.
