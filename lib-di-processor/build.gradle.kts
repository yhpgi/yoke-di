plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.dokka)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
  implementation(project(":lib-di-annotation"))
  implementation(libs.ksp.api)
  implementation(libs.kotlinpoet.core)
  implementation(libs.kotlinpoet.ksp)
}


