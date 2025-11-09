# Panduan Memulai

Mengintegrasikan Yoke DI ke dalam proyek Kotlin Multiplatform Anda sangatlah mudah. Ikuti langkah-langkah di bawah ini untuk memulai.

## Langkah 1: Konfigurasi Gradle

Yoke DI menggunakan KSP (Kotlin Symbol Processing) untuk menghasilkan kode. Anda perlu menambahkan plugin KSP dan dependensi Yoke ke proyek Anda.

!!! info "Status Publikasi"
    Saat ini, Yoke DI belum dipublikasikan ke Maven Central. Panduan ini mengasumsikan Anda telah menyertakan `lib-di`, `lib-di-annotation`, dan `lib-di-processor` sebagai modul lokal di proyek Anda.

### 1. Tambahkan Plugin KSP

Pastikan Anda memiliki plugin KSP di file `build.gradle.kts` level proyek (root).

```kotlin title="build.gradle.kts (proyek)"
plugins {
    alias(libs.plugins.ksp) apply false
    // ... plugin lainnya
}
```

### 2. Terapkan Plugin dan Tambahkan Dependensi

Di file `build.gradle.kts` modul *common* Anda, terapkan plugin KSP dan tambahkan dependensi Yoke.

```kotlin title="sample/build.gradle.kts (modul)"
plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp) // Terapkan KSP
    // ... plugin lainnya
}

kotlin {
    // ... konfigurasi KMP Anda

    sourceSets {
        commonMain.dependencies {
            implementation(project(":lib-di-annotation"))
            implementation(project(":lib-di"))
        }
    }
}

val kspTaskName = "kspCommonMainKotlinMetadata"
tasks.matching { it.name.startsWith("ksp") && it.name != kspTaskName }.configureEach { dependsOn(kspTaskName) }

kotlin.sourceSets.commonMain {
     kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

// Tambahkan prosesor KSP untuk commonMain
dependencies {
    add("kspCommonMainMetadata", project(":lib-di-processor"))
}
```

## Langkah 2: "Hello, Yoke!"

Mari kita buat contoh DI sederhana.

### 1. Definisikan Komponen Root

Komponen adalah inti dari grafik dependensi Anda. Buat sebuah `interface` dan tandai dengan `@YokeComponent` dan `@YokeEntryPoint`.

```kotlin title="di/AppComponent.kt"
package io.github.yhpgi.yoke.sample.di

import io.github.yhpgi.yoke.annotation.*

@YokeEntryPoint
@Singleton
@YokeComponent
interface AppComponent
```

- `@YokeComponent`: Mendefinisikan `interface` ini sebagai container DI.
- `@Singleton`: Menetapkan *scope* default untuk komponen ini.
- `@YokeEntryPoint`: Memberi tahu Yoke bahwa ini adalah komponen root yang akan digunakan untuk menghasilkan `YokeApplication`.

### 2. Buat Dependensi

Buat kelas sederhana yang ingin Anda injeksi. Gunakan `@Injectable` pada konstruktor dan `@ContributesTo` untuk mendaftarkannya ke komponen.

```kotlin title="domain/Greeter.kt"
package io.github.yhpgi.yoke.sample.domain

import io.github.yhpgi.yoke.annotation.ContributesTo
import io.github.yhpgi.yoke.annotation.Injectable
import io.github.yhpgi.yoke.sample.di.AppComponent

@Injectable
@ContributesTo(AppComponent::class)
class Greeter {
    fun greet(): String = "Hello from Yoke DI!"
}
```

### 3. Inisialisasi dan Injeksi

Terakhir, bungkus UI Anda dengan `YokeApplication` dan gunakan `inject<T>()` untuk mendapatkan dependensi Anda.

```kotlin title="App.kt"
package io.github.yhpgi.yoke.sample

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.yhpgi.yoke.di.YokeApplication
import io.github.yhpgi.yoke.di.inject
import io.github.yhpgi.yoke.sample.domain.Greeter

@Composable
fun App() {
    // YokeApplication menginisialisasi DI container
    YokeApplication {
        // inject<Greeter>() akan menyediakan instance Greeter
        val greeter = inject<Greeter>()

        Text(text = greeter.greet())
    }
}
```

## Langkah 3: Build Proyek Anda

Jalankan build Gradle. KSP akan berjalan dan menghasilkan kode yang diperlukan di direktori `build/generated`. Setelah build berhasil, aplikasi Anda siap dijalankan dengan Yoke DI!

Sekarang Anda siap untuk menjelajahi [Konsep Inti](./core-concepts/components.md) Yoke DI lebih dalam.
