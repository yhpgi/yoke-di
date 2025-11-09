package io.github.yhpgi.yoke.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP entry point that provides instances of the [YokeProcessor].
 * This class is registered in `META-INF/services` and is discovered by KSP at build time.
 */
class YokeProcessorProvider : SymbolProcessorProvider {

  /**
   * Creates a new instance of the [YokeProcessor].
   *
   * @param environment The KSP environment, providing access to the code generator, logger, and options.
   * @return A new [YokeProcessor] instance.
   */
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    return YokeProcessor(
      codeGenerator = environment.codeGenerator,
      logger = environment.logger
    )
  }
}
