# Selamat Datang di Yoke DI

<p align="center">
  <img src="https://raw.githubusercontent.com/yhpgi/yoke-di/main/art/yoke-logo.png" alt="Yoke DI Logo" width="200"/>
</p>

<p align="center">
  <strong>Kerangka kerja Dependency Injection (DI) yang ringan, berbasis KSP, dan modern untuk Kotlin Multiplatform & Compose Multiplatform.</strong>
</p>

---

**Yoke DI** adalah solusi dependency injection yang dirancang untuk menyederhanakan pengembangan aplikasi Kotlin Multiplatform (KMP). Dengan memanfaatkan kekuatan Kotlin Symbol Processing (KSP), Yoke DI menghasilkan kode DI yang efisien saat *compile-time*, menghilangkan kebutuhan akan *reflection* dan memastikan performa maksimal.

Terinspirasi dari Dagger/Hilt, Yoke DI membawa konsep-konsep yang sudah terbukti ke dunia KMP dengan API yang lebih sederhana, lebih intuitif, dan terintegrasi secara mendalam dengan Compose Multiplatform.

## Mengapa Memilih Yoke DI?

- ✅ **Sederhana dan Intuitif**: Mengurangi *boilerplate* dan kompleksitas yang sering ditemukan pada kerangka kerja DI lainnya.
- ✅ **Performa Tinggi**: Tidak ada *reflection*. Semua dependensi diresolusi saat *compile-time*.
- ✅ **Benar-benar Multiplatform**: Satu set anotasi dan API untuk semua target Anda: Android, iOS (mendatang), Desktop, Web.
- ✅ **API DSL Modern**: Gunakan `inject<T>()`, `injectViewModel<T>()`, dan `scope<T> {}` untuk kode yang lebih bersih dan ekspresif.

## Siap Memulai?

Langsung saja ke [**Panduan Memulai**](./getting-started.md) untuk mengintegrasikan Yoke DI ke dalam proyek Anda dalam beberapa menit.
