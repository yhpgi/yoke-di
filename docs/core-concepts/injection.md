# Konsep Inti: Injeksi Dependensi

Yoke DI menyediakan beberapa cara untuk mendeklarasikan dan menginjeksi dependensi, semuanya dirancang agar mudah digunakan dan *type-safe*.

## Constructor Injection dengan `@Injectable`

Cara paling umum untuk memberitahu Yoke cara membuat sebuah objek adalah dengan menandai konstruktornya dengan `@Injectable`.

```kotlin
@Injectable
@ContributesTo(AppComponent::class)
class MyRepository(
    private val apiService: ApiService // Yoke akan menyediakan ApiService juga
) {
    // ...
}
```

Ketika Yoke perlu menyediakan `MyRepository`, ia akan mencari konstruktor `@Injectable`, melihat bahwa ia membutuhkan `ApiService`, lalu mencari cara untuk menyediakan `ApiService`, dan begitu seterusnya hingga semua dependensi terpenuhi.

## Menginjeksi Dependensi dengan DSL

Yoke menyediakan API berbasis DSL yang modern dan intuitif untuk mendapatkan dependensi Anda.

### `inject<T>()`

Di dalam Composable, gunakan `inject<T>()` untuk mendapatkan instance dependensi Anda. Hasilnya akan diingat (`remembered`) di seluruh recomposition.

```kotlin
@Composable
fun MyScreen() {
    val repository = inject<MyRepository>()
    // ... gunakan repository
}
```

### `injectViewModel<T>()`

Untuk ViewModels, gunakan `injectViewModel<T>()`. Ini secara otomatis menangani pembuatan dan scoping ViewModel ke `ViewModelStoreOwner` yang benar.

```kotlin
@Injectable
@ContributesTo(AppComponent::class)
class MyViewModel(
    private val repository: MyRepository
) : ViewModel() {
    // ...
}

@Composable
fun MyScreen() {
    val viewModel = injectViewModel<MyViewModel>()
    // ... gunakan viewModel
}
```

## Binding Interface dengan `@Binds`

Praktik terbaik dalam pengembangan perangkat lunak adalah bergantung pada abstraksi (interface) daripada implementasi konkret. `@Binds` memungkinkan Anda melakukan ini.

Anda dapat menandai kelas implementasi dengan `@Binds` untuk memberitahu Yoke implementasi mana yang harus disediakan ketika sebuah interface diminta.

**1. Definisikan Interface**

```kotlin
interface AuthRepository {
    fun isLoggedIn(): Boolean
}
```

**2. Anotasi Implementasi**

```kotlin
@Singleton
@Injectable
@Binds(to = AuthRepository::class) // Mengikat AuthRepositoryImpl ke AuthRepository
@ContributesTo(AppComponent::class)
class AuthRepositoryImpl : AuthRepository {
    override fun isLoggedIn(): Boolean = true
}
```

**3. Injeksi Interface**

Sekarang Anda dapat menginjeksi `AuthRepository` dan Yoke akan secara otomatis menyediakan `AuthRepositoryImpl`.

```kotlin
@Injectable
@ContributesTo(AppComponent::class)
class MyViewModel(
    private val authRepository: AuthRepository // Yoke menyediakan AuthRepositoryImpl
) : ViewModel() {
    // ...
}
```
