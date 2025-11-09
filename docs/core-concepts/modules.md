# Konsep Inti: Modul & Provides

Terkadang, Anda tidak dapat menggunakan *constructor injection* dengan `@Injectable`. Ini biasanya terjadi ketika:

-   Anda perlu membuat instance dari kelas yang berasal dari library eksternal (Anda tidak memiliki kodenya).
-   Anda perlu melakukan konfigurasi khusus untuk membuat sebuah objek (misalnya, menggunakan pola *builder*).
-   Anda perlu menyediakan implementasi untuk sebuah interface, dan implementasinya tidak memiliki konstruktor `@Injectable`.

Di sinilah **Modul** dan `@Provides` berperan.

## `@Module`

Modul adalah `object` Kotlin yang ditandai dengan `@Module`. Modul berfungsi sebagai wadah untuk fungsi-fungsi `@Provides`.

```kotlin
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Module

@Module
@ContributesTo(AppComponent::class) // Modul ini berkontribusi ke AppComponent
object DataModule {
    // ... fungsi @Provides di sini
}
```

Sama seperti dependensi lainnya, modul harus dikaitkan ke sebuah komponen menggunakan `@ContributesTo`.

## `@Provides`

Di dalam `@Module`, Anda dapat mendefinisikan fungsi yang ditandai dengan `@Provides`. Fungsi-fungsi ini memberitahu Yoke cara membuat dan menyediakan suatu dependensi.

-   **Tipe kembalian** dari fungsi `@Provides` memberitahu Yoke tipe apa yang disediakan oleh fungsi ini.
-   **Parameter** dari fungsi adalah dependensi yang dibutuhkan oleh fungsi itu sendiri. Yoke akan menyediakan parameter-parameter ini secara otomatis.

### Contoh: Menyediakan Library Eksternal

Misalkan Anda menggunakan library `Retrofit` dan perlu menyediakan sebuah instance.

```kotlin
// Kelas dari library eksternal, tidak bisa kita anotasi
class Retrofit private constructor(...) {
    class Builder { /* ... */ }
}

// Modul kita
@Module
@ContributesTo(AppComponent::class)
object NetworkModule {

    @Provides
    @Singleton // Kita juga bisa memberikan scope di sini
    fun provideRetrofitInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .build()
    }
}
```

### Contoh: Menyediakan Dependensi dengan Dependensi Lain

Fungsi `@Provides` juga bisa memiliki dependensi.

```kotlin
@Module
@ContributesTo(AppComponent::class)
object ApiModule {

    // Yoke akan menyediakan 'retrofit' dari NetworkModule
    @Provides
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
}
```

Sekarang, setiap kali Anda meminta `AuthApiService`, Yoke akan memanggil `provideAuthApiService` dan secara otomatis menyediakan parameter `retrofit` yang dibutuhkan.
