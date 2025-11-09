# Penggunaan Lanjutan: Integrasi Android

Yoke DI menyediakan beberapa fitur khusus untuk menyederhanakan pengembangan di platform Android.

## Inisialisasi Otomatis dengan `YokeContentProvider`

Cara termudah untuk menginisialisasi Yoke di Android adalah secara otomatis. Yoke menyediakan `ContentProvider` internal yang akan menangkap `Context` aplikasi saat startup.

Untuk mengaktifkannya, cukup tambahkan `provider` ke `AndroidManifest.xml` Anda.

```xml title="AndroidManifest.xml"
<manifest ...>
    <application ...>

        <!-- Yoke Content Provider untuk inisialisasi otomatis -->
        <provider
            android:name="io.github.yhpgi.yoke.di.YokeContentProvider"
            android:authorities="${applicationId}.yoke.di.provider"
            android:exported="false"
            android:initOrder="101" />

    </application>
</manifest>
```

Dengan ini, Yoke akan siap untuk injeksi global (`injectGlobal`) bahkan sebelum UI pertama ditampilkan.

## Inisialisasi Manual

Jika Anda lebih suka kontrol manual atau tidak dapat menggunakan `ContentProvider`, Anda bisa menginisialisasi Yoke secara manual di kelas `Application` Anda.

```kotlin title="MyApplication.kt"
import android.app.Application
import io.github.yhpgi.yoke.di.AndroidYoke

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidYoke.initialize(this)
    }
}
```

## Menginjeksi `Context`

Seringkali Anda memerlukan `Context` aplikasi sebagai dependensi (misalnya, untuk mengakses SharedPreferences, database, dll.). Anda bisa menyediakannya melalui modul.

```kotlin title="di/PlatformModule.android.kt"
import android.content.Context
import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Module
import io.github.yhpgi.yoke.annotation.Provides
import io.github.yhpgi.yoke.annotation.Singleton
import io.github.yhpgi.yoke.di.AndroidYoke

@Module
@ContributesTo(AppComponent::class)
object PlatformModule {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context {
        return AndroidYoke.getApplicationContext()
    }
}
```

Sekarang Anda bisa menginjeksi `Context` di mana saja.

## Fungsi Ekstensi `Context.inject<T>()`

Untuk kenyamanan, Yoke menyediakan fungsi ekstensi pada `Context` untuk melakukan injeksi global. Ini adalah alternatif dari `injectGlobal<T>()` yang mungkin terasa lebih alami di lingkungan Android.

```kotlin
import io.github.yhpgi.yoke.di.inject

class MyActivity : AppCompatActivity() {
    // Injeksi menggunakan context aplikasi
    private val myRepository: MyRepository by lazy {
        applicationContext.inject()
    }

    // Injeksi dengan qualifier
    private val premiumRepository: UserRepository by lazy {
        applicationContext.inject {
            qualifiedBy(PremiumUser::class)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
    }
}
```
