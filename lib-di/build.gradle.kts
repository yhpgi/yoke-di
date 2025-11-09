@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.dokka)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.maven.publish)
}

kotlin {
  androidTarget {
    publishLibraryVariants("release")
    compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
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
    api(project(":lib-di-annotation"))
    api(compose.runtime)
    api(libs.jetbrains.lifecycle.viewmodel)
  }
}

android {
  namespace = "io.github.yhpgi.yoke.di"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
}


