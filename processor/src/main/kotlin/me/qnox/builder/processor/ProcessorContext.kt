package me.qnox.builder.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.processor.bean.BeanIntrospector
import me.qnox.builder.processor.generators.BeanGenerator
import me.qnox.builder.processor.generators.ListGenerator
import me.qnox.builder.processor.generators.SimpleGenerator

class ProcessorContext(
    val resolver: Resolver,
    private val annotations: Set<String>,
    val extensions: Extensions,
) {
    val introspector = BeanIntrospector()

    private val generators =
        listOf(
            ListGenerator(),
            BeanGenerator(),
        )

    private val fallbackGenerator = SimpleGenerator()

    fun builderClassName(s: KSClassDeclaration) =
        ClassName(s.packageName.asString(), s.simpleName.asString() + "Builder")

    fun dslInterfaceName(s: KSClassDeclaration) = ClassName(s.packageName.asString(), s.simpleName.asString() + "Dsl")

    fun getPropertyTypeName(propertyType: KSTypeReference): TypeName {
        val resolvedType = propertyType.resolve()
        return when (val declaration = resolvedType.declaration) {
            is KSClassDeclaration -> {
                val generator = getGenerator(declaration)
                generator.generateTypeName(this, resolvedType)
            }
            else -> {
                propertyType.toTypeName()
            }
        }
    }

    private fun getGenerator(classDeclaration: KSClassDeclaration): Generator = generators.find {
        it.supports(this, classDeclaration)
    } ?: fallbackGenerator

    internal fun getGenerator(type: KSTypeReference): Generator {
        val classDeclaration = type.resolve().declaration as KSClassDeclaration
        return getGenerator(classDeclaration)
    }

    fun isAnnotated(ksClassDeclaration: KSClassDeclaration): Boolean = ksClassDeclaration.annotations.any {
        annotations.contains(resolveAnnotationName(it))
    }

    private fun resolveAnnotationName(it: KSAnnotation) =
        (it.annotationType.resolve().declaration as KSClassDeclaration).qualifiedName?.asString()
}
