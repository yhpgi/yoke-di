# Konsep Inti: Assisted Injection

*Assisted Injection* adalah pola yang berguna ketika sebuah kelas membutuhkan beberapa dependensi dari grafik DI, tetapi juga memerlukan beberapa parameter yang hanya tersedia saat *runtime*. Ini pada dasarnya adalah cara untuk membuat *factory* secara otomatis.

Misalnya, bayangkan sebuah `ListItemFormatter` yang membutuhkan `AnalyticsService` dari DI, tetapi juga `id` dan `prefix` item yang akan diformat, yang diberikan saat runtime.

## `@Assisted`

Gunakan anotasi `@Assisted` untuk menandai parameter di konstruktor `@Injectable` yang ingin Anda berikan saat runtime.

```kotlin
import io.github.yhpgi.yoke.annotation.Assisted
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.domain.AnalyticsService

@Injectable
@ContributesTo(AppComponent::class)
class ListItemFormatter(
    private val analyticsService: AnalyticsService, // Dari DI
    @Assisted private val prefix: String,           // Diberikan saat runtime
    @Assisted private val id: Int,                  // Diberikan saat runtime
) {
    // ...
}
```

## Mendefinisikan Factory

Selanjutnya, Anda perlu mendefinisikan sebuah `interface` `Factory` di dalam kelas tersebut.

-   Nama `interface` harus **tepat** `Factory`.
-   `interface` harus memiliki **satu** fungsi, yang biasanya bernama `create`.
-   Fungsi `create` harus mengembalikan instance dari kelas yang melingkupinya (`ListItemFormatter`).
-   Parameter dari fungsi `create` harus **cocok persis** (dalam urutan dan tipe) dengan parameter `@Assisted` di konstruktor.

```kotlin
@Injectable
@ContributesTo(AppComponent::class)
class ListItemFormatter(
    private val analyticsService: AnalyticsService,
    @Assisted private val prefix: String,
    @Assisted private val id: Int,
) {
    fun format(): String {
        analyticsService.trackEvent("formatListItem")
        return "$prefix: Item ID #$id has been formatted."
    }

    // Definisikan Factory di sini
    interface Factory {
        fun create(prefix: String, id: Int): ListItemFormatter
    }
}
```

Yoke akan secara otomatis menemukan `interface` ini dan menghasilkan implementasi untuknya.

## Menggunakan Factory

Anda tidak menginjeksi kelas dengan parameter `@Assisted` secara langsung. Sebaliknya, Anda menginjeksi `Factory`-nya.

```kotlin
@Injectable
@ContributesTo(AppComponent::class)
class MyViewModel(
    // Injeksi Factory, bukan ListItemFormatter
    private val formatterFactory: ListItemFormatter.Factory
) : ViewModel() {

    fun formatAnItem() {
        // Gunakan factory untuk membuat instance dengan parameter runtime
        val formatter = formatterFactory.create(prefix = "Item", id = 123)
        val formattedText = formatter.format()
        println(formattedText) // -> "Item: Item ID #123 has been formatted."
    }
}
```

Dengan menginjeksi `Factory`, Anda dapat membuat sebanyak mungkin instance `ListItemFormatter` yang Anda butuhkan, masing-masing dengan data runtime yang berbeda, sambil tetap membiarkan Yoke menyediakan dependensi yang dibutuhkan (seperti `AnalyticsService`).
