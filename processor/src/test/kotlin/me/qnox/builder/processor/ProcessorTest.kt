package me.qnox.builder.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.test.Test

@OptIn(ExperimentalCompilerApi::class)
class ProcessorTest {

    @Test
    fun `test builder annotation processor`() {
        val result = compile(
            SourceFile.kotlin(
                "KClass.kt",
                """
                    import me.qnox.builder.Builder
        
                    @Builder
                    class KClass(val attr: Int)
                """,
            ),
        )
        val generatedBuilder = result.classLoader.loadClass("KClassBuilder")
        generatedBuilder.getDeclaredMethod("attr", Int::class.java).shouldNotBeNull()
    }

    @Test
    fun `test builder annotation processor with builder attr type`() {
        val result = compile(
            SourceFile.kotlin(
                "KClass.kt",
                """
                    import me.qnox.builder.Builder
    
                    @Builder
                    class KClass(val attr: KAttrClass)
                """,
            ),
            SourceFile.kotlin(
                "KAttrClass.kt",
                """
                    import me.qnox.builder.Builder
    
                    @Builder
                    class KAttrClass(val attr: Int)
                """,
            ),
        )
        val generatedBuilder = result.classLoader.loadClass("KClassBuilder")
        generatedBuilder.getDeclaredMethod("attr", Function1::class.java).shouldNotBeNull()
    }

    private fun compile(vararg typeSource: SourceFile): KotlinCompilation.Result {
        val compilation = KotlinCompilation().apply {
            sources = listOf(*typeSource)
            symbolProcessorProviders = listOf(ProcessorProvider())
            inheritClassPath = true
            kspWithCompilation = true
        }
        val result = compilation.compile()

        result.exitCode.shouldBe(KotlinCompilation.ExitCode.OK)
        return result
    }
}
