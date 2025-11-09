# Yoke DI

<p align="center">
  <img src="https://raw.githubusercontent.com/yhpgi/yoke-di/main/art/yoke-logo.png" alt="Yoke DI Logo" width="200"/>
</p>

<p align="center">
  <strong>Kerangka kerja Dependency Injection (DI) yang ringan, berbasis KSP, dan modern untuk Kotlin Multiplatform & Compose Multiplatform.</strong>
</p>

<p align="center">
    <a href="https://github.com/yhpgi/yoke-di/actions/workflows/build.yml"><img src="https://github.com/yhpgi/yoke-di/actions/workflows/build.yml/badge.svg" alt="Build Status"></a>
    <a href="https://github.com/yhpgi/yoke-di/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT"></a>
</p>

---

Yoke DI terinspirasi dari konsep Dagger/Hilt yang sudah terbukti, tetapi dibangun dari awal untuk ekosistem Kotlin Multiplatform (KMP) modern. Yoke DI bertujuan untuk menyederhanakan dependency injection di proyek KMP Anda dengan menyediakan API yang aman (*type-safe*), boilerplate minimal, dan integrasi yang mulus dengan Jetpack Compose.

## Fitur Utama

- 🚀 **KMP First**: Didesain dari awal untuk KMP, mendukung **Android, JVM (Desktop), JavaScript, dan WasmJs**.
- ⚡️ **Berbasis KSP**: Menggunakan Kotlin Symbol Processing (KSP) untuk pemrosesan anotasi yang cepat dan efisien, menghasilkan kode saat *compile-time* tanpa *reflection*.
- ✨ **API DSL yang Ekspresif**: API yang modern, intuitif, dan *type-safe* untuk injeksi, manajemen *scope*, dan lainnya.
- 🎨 **Integrasi Compose**: Integrasi kelas satu dengan Jetpack & Compose Multiplatform, termasuk `injectViewModel()` dan manajemen *scope* berbasis Composable.
- 🎯 **Terinspirasi dari Konsep Populer**: Mengadopsi konsep yang sudah dikenal seperti Komponen, Modul, dan *Scope* dari Dagger/Hilt, tetapi dengan implementasi yang jauh lebih sederhana.

## Penyiapan

Yoke DI belum dipublikasikan ke Maven Central. Untuk saat ini, Anda bisa menambahkannya sebagai modul lokal di proyek Anda.

1.  **Tambahkan plugin KSP** ke file `build.gradle.kts` level proyek Anda.

    ```kts
    // build.gradle.kts (project level)
    plugins {
      alias(libs.plugins.ksp) version "..." apply false
    }
    ```

2.  **Terapkan plugin KSP** dan tambahkan dependensi Yoke di file `build.gradle.kts` modul Anda.

    ```kts
    // build.gradle.kts (module level)
    plugins {
      // ...
      alias(libs.plugins.ksp)
    }

    kotlin {
      sourceSets {
        commonMain.dependencies {
          implementation(project(":lib-di-annotation"))
          implementation(project(":lib-di"))
          ...
        }
      }
    }

    val kspTaskName = "kspCommonMainKotlinMetadata"
    tasks.matching { it.name.startsWith("ksp") && it.name != kspTaskName }.configureEach { dependsOn(kspTaskName) }

    kotlin.sourceSets.commonMain {
      kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    dependencies {
      // ...
      add("kspCommonMainMetadata", ":lib-di-processor")
    }
    ```

## Penggunaan Cepat

Berikut adalah gambaran singkat cara kerja Yoke:

**1. Definisikan Komponen Root**
Tandai sebuah `interface` sebagai titik masuk (*entry point*) untuk grafik dependensi Anda.

```kotlin
@YokeEntryPoint
@Singleton
@YokeComponent
interface AppComponent
```

**2. Buat Kelas yang Dapat Diinjeksi**
Gunakan `@Injectable` pada konstruktor kelas Anda dan `@ContributesTo` untuk mengaitkannya ke sebuah komponen.

```kotlin
@Injectable
@ContributesTo(AppComponent::class)
class Greeter {
  fun greet(): String = "Hello from Yoke DI!"
}
```

**3. Inisialisasi Yoke di Aplikasi Anda**
Bungkus UI root Anda dengan Composable `YokeApplication`.

```kotlin
// Di root Composable Anda (mis. App.kt)
@Composable
fun App() {
  YokeApplication {
    // ... UI Anda di sini
  }
}
```

**4. Injeksi Dependensi Anda**
Gunakan DSL `inject()` di dalam Composable untuk mendapatkan instance dari dependensi Anda.

```kotlin
@Composable
fun MyScreen() {
  // Yoke akan menyediakan instance Greeter secara otomatis
  val greeter = inject<Greeter>()

  Text(greeter.greet())
}
```

## Konsep Inti

| Fitur | Anotasi / DSL | Deskripsi |
| :--- | :--- | :--- |
| **Constructor Injection** | `@Injectable` | Memberi tahu Yoke cara membuat sebuah kelas. |
| **Komponen** | `@YokeComponent` | Mendefinisikan sebuah container DI (grafik dependensi). |
| **Subkomponen** | `@YokeSubcomponent` | Komponen dengan siklus hidup yang lebih pendek (mis., scope user). |
| **Titik Masuk** | `@YokeEntryPoint` | Menandai komponen root untuk pembuatan `YokeApplication`. |
| **Kontribusi** | `@ContributesTo` | Menghubungkan dependensi atau subkomponen ke sebuah komponen. |
| **Modul & Provides** | `@Module`, `@Provides` | Menyediakan instance dari kelas eksternal atau yang butuh konfigurasi. |
| **Binding Interface** | `@Binds` | Mengikat sebuah implementasi ke interfacenya. |
| **Scoping** | `@Singleton`, `@Scope` | Mengikat siklus hidup dependensi ke komponennya. |
| **Qualifiers** | `@YokeQualifier`, `@QualifiedBy`| Membedakan antara beberapa implementasi dari interface yang sama. |
| **Assisted Injection** | `@Assisted`, `Factory` | Membuat factory untuk kelas yang membutuhkan parameter runtime. |

## Dokumentasi

Untuk panduan yang lebih mendalam, contoh, dan referensi API, silakan kunjungi **[dokumentasi](https://yhpgi.github.io/yoke-di/)**.

## Aplikasi Contoh

Proyek ini menyertakan [modul `sample`](./sample) yang mendemonstrasikan semua fitur utama Yoke DI dalam aplikasi KMP yang berfungsi penuh.

## Kontribusi

Kontribusi dalam bentuk apa pun sangat kami hargai! Jika Anda ingin membantu, silakan buka *issue* untuk mendiskusikan perubahan atau ajukan *pull request*.

## Lisensi

Yoke DI dilisensikan di bawah **[Lisensi MIT](https://github.com/yhpgi/yoke-di/blob/main/LICENSE)**.
