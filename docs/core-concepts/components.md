# Konsep Inti: Komponen

Komponen adalah jantung dari Yoke DI. Mereka bertindak sebagai *container* atau "pabrik" untuk dependensi Anda. Setiap komponen mendefinisikan sebuah grafik dependensi dan siklus hidupnya.

## `@YokeComponent`

Anotasi `@YokeComponent` digunakan untuk mendefinisikan sebuah container DI. Biasanya, Anda akan memiliki setidaknya satu komponen root di aplikasi Anda.

Sebuah komponen didefinisikan sebagai sebuah `interface`.

```kotlin
import io.github.yhpgi.yoke.annotation.Singleton
import io.github.yhpgi.yoke.annotation.YokeComponent

@Singleton
@YokeComponent
interface AppComponent
```

- **Scope**: Anda dapat memberikan anotasi *scope* (seperti `@Singleton`) pada komponen. Ini menetapkan *scope* default untuk semua dependensi di dalamnya yang tidak memiliki *scope* sendiri.

## `@YokeEntryPoint`

Anotasi ini menandai sebuah komponen sebagai titik masuk utama untuk aplikasi Anda. Yoke akan menggunakan komponen ini untuk menghasilkan Composable `YokeApplication` yang membungkus seluruh aplikasi Anda.

**Hanya boleh ada satu `@YokeEntryPoint` per aplikasi.**

```kotlin
import io.github.yhpgi.yoke.annotation.YokeEntryPoint

@YokeEntryPoint // Menandai ini sebagai komponen root utama
@Singleton
@YokeComponent
interface AppComponent
```

## `@YokeSubcomponent`

Subkomponen adalah komponen yang siklus hidupnya lebih pendek dan terikat pada komponen induk. Mereka berguna untuk membuat *scope* yang lebih terbatas, seperti sesi pengguna, alur fitur, atau layar tertentu.

Subkomponen juga didefinisikan sebagai `interface` dan harus mewarisi `DIContainer`.

```kotlin
import io.github.yhpgi.yoke.annotation.Scope
import io.github.yhpgi.yoke.annotation.YokeSubcomponent
import io.github.yhpgi.yoke.di.DIContainer

@Scope
annotation class UserScope // Scope kustom

@UserScope
@YokeSubcomponent
interface UserComponent : DIContainer
```

## `@ContributesTo`

Anotasi `@ContributesTo` adalah perekat yang menghubungkan semuanya. Anotasi ini digunakan pada:

1.  **Dependensi** (`@Injectable`, `@Module`) untuk mendaftarkannya ke sebuah komponen.
2.  **Subkomponen** (`@YokeSubcomponent`) untuk mengaitkannya dengan komponen induknya.

### Menghubungkan Dependensi

```kotlin
// Greeter akan tersedia di dalam AppComponent
@Injectable
@ContributesTo(AppComponent::class)
class Greeter { /* ... */ }
```

### Menghubungkan Subkomponen

```kotlin
// UserComponent adalah subkomponen dari AppComponent
@UserScope
@YokeSubcomponent
@ContributesTo(AppComponent::class)
interface UserComponent : DIContainer
```

Dengan menghubungkan `UserComponent` ke `AppComponent`, dependensi di dalam `UserComponent` dapat mengakses dependensi yang disediakan oleh `AppComponent`.
