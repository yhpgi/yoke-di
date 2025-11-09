@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.dokka)
  alias(libs.plugins.multiplatform)
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose.hotreload)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  jvmToolchain(21)
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
    }
  }
  jvm()
  js(IR) {
    browser()
    binaries.executable()
  }
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(project(":lib-di-annotation"))
      implementation(project(":lib-di"))
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.materialIconsExtended)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.navigation.compose)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.jetbrains.lifecycle.viewmodel)

    }
    androidMain.dependencies {
      implementation(libs.androidx.appcompat)
      implementation(libs.androidx.activity.compose)
      implementation(libs.androidx.work.runtime.ktx)

    }
    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
    }

  }

  sourceSets.all {
    languageSettings {
      optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    }
  }
}

android {
  namespace = "io.github.yhpgi.yoke.sample"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    applicationId = "io.github.yhpgi.yoke.sample.android"
    versionCode = 1
    versionName = "1.0"
  }

  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debug")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
}

val kspTaskName = "kspCommonMainKotlinMetadata"
tasks.matching { it.name.startsWith("ksp") && it.name != kspTaskName }.configureEach { dependsOn(kspTaskName) }

kotlin.sourceSets.commonMain {
  kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}

dependencies {
  val diProcessor = project(":lib-di-processor")
  add("kspCommonMainMetadata", diProcessor)
}

compose.desktop {
  application {
    mainClass = "io.github.yhpgi.yoke.sample.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "io.github.yhpgi.yoke.sample"
      packageVersion = "1.0.0"
    }
  }
}


