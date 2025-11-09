plugins {
  alias(libs.plugins.multiplatform) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.maven.publish) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.dokka)
}

dependencies {
  dokka(project(":lib-di"))
  dokka(project(":lib-di-annotation"))
  dokka(project(":lib-di-processor"))
  dokka(project(":sample"))
}
