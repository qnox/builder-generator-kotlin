package me.qnox.builder.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import me.qnox.builder.ListDsl
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test

@OptIn(ExperimentalCompilerApi::class)
class ProcessorTest {
    @Test
    fun `test builder annotation processor`() {
        val result =
            compile(
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
    fun `should support meta annotations`() {
        val result =
            compile(
                SourceFile.kotlin(
                    "KClass.kt",
                    """
                    import me.qnox.builder.Builder
        
                    @Builder
                    annotation class Meta 

                    @Meta
                    class KClass(val attr: Int)
                """,
                ),
            )
        val generatedBuilder = result.classLoader.loadClass("KClassBuilder")
        generatedBuilder.getDeclaredMethod("attr", Int::class.java).shouldNotBeNull()
    }

    @Test
    fun `test builder annotation processor with builder attr type`() {
        val result =
            compile(
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

    @Test
    fun `builder should support collections`() {
        val result =
            compile(
                SourceFile.kotlin(
                    "KClass.kt",
                    """
                    import me.qnox.builder.Builder
    
                    @Builder
                    class KClass(val list: List<KSubClass>)
                """,
                ),
                SourceFile.kotlin(
                    "KSubClass.kt",
                    """
                    import me.qnox.builder.Builder
    
                    @Builder
                    class KSubClass(val attr: Int)
                """,
                ),
            )
        val generatedBuilder = result.classLoader.loadClass("KClassBuilder").kotlin
        val generatedSubclassBuilder = result.classLoader.loadClass("KSubClassBuilder").kotlin
        generatedBuilder.memberFunctions.filter { it.name == "list" }.shouldHaveSingleElement {
            it.parameters.size == 2 &&
                it.parameters[1]
                    .type.arguments[0]
                    .type ==
                ListDsl::class.createType(
                    listOf(
                        KTypeProjection(
                            KVariance.INVARIANT,
                            generatedSubclassBuilder.createType(),
                        ),
                    ),
                )
        }
    }

    private fun compile(vararg typeSource: SourceFile): KotlinCompilation.Result {
        val compilation =
            KotlinCompilation().apply {
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
