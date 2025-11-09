@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.dokka)
  alias(libs.plugins.maven.publish)
}

kotlin {
  androidTarget {
    publishLibraryVariants("release")
    compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
  }
  jvm()
  js(IR) {
    browser()
    nodejs()
  }
  wasmJs {
    browser()
  }

  sourceSets.commonMain.dependencies {
    api(libs.kotlin.reflect)
  }
}

android {
  namespace = "io.github.yhpgi.yoke.annotation"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}


