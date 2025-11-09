# Penggunaan Lanjutan: Injeksi Non-Composable

Meskipun Yoke DI dirancang dengan mempertimbangkan Compose, ada banyak skenario di mana Anda perlu mengakses dependensi di luar fungsi `@Composable`. Contohnya termasuk:

-   *Background worker*
-   Service
-   Kelas *helper* atau utilitas
-   Logika bisnis di luar ViewModel

Untuk kasus-kasus ini, Yoke menyediakan fungsi `injectGlobal<T>()`.

## `injectGlobal<T>()`

Fungsi `injectGlobal<T>()` memungkinkan Anda untuk mendapatkan dependensi dari mana saja di aplikasi Anda, selama grafik dependensi telah diinisialisasi.

!!! warning "Prasyarat"
    `injectGlobal<T>()` hanya akan berfungsi **setelah** Composable `YokeApplication` telah ditampilkan setidaknya satu kali. Ini karena `YokeApplication` bertanggung jawab untuk membangun dan menginisialisasi container DI global. Memanggil `injectGlobal` sebelum itu akan menyebabkan *crash*.

### Penggunaan Dasar

Cukup panggil `injectGlobal<T>()` dengan tipe yang ingin Anda dapatkan.

```kotlin
import io.github.yhpgi.yoke.di.injectGlobal
import io.github.yhpgi.yoke.sample.domain.AnalyticsService

class SampleWorker {
    fun doWork() {
        // Dapatkan AnalyticsService dari container global
        val analytics = injectGlobal<AnalyticsService>()
        analytics.trackEvent("Worker task started")
    }
}
```

### Penggunaan dengan Qualifier

Sama seperti `inject<T>()`, Anda juga dapat menggunakan `qualifiedBy` untuk mendapatkan implementasi yang spesifik.

```kotlin
import io.github.yhpgi.yoke.di.injectGlobal
import io.github.yhpgi.yoke.sample.di.PremiumUser
import io.github.yhpgi.yoke.sample.domain.UserRepository

class UserManager {
    fun getPremiumUser(): String {
        val premiumRepo = injectGlobal<UserRepository> {
            qualifiedBy(PremiumUser::class)
        }
        return premiumRepo.getUsername()
    }
}
```

Fungsi `injectGlobal<T>()` adalah alat yang ampuh untuk mengintegrasikan Yoke DI ke seluruh bagian dari aplikasi Anda, tidak hanya di lapisan UI.
