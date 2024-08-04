package me.qnox.builder.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import me.qnox.builder.processor.bean.BeanIntrospector
import me.qnox.builder.processor.generators.BeanPropertyGenerator
import me.qnox.builder.processor.generators.ListPropertyGenerator
import me.qnox.builder.processor.generators.SimplePropertyGenerator

class ProcessorContext(
    val resolver: Resolver,
    private val annotations: Set<String>,
    val extensions: Extensions,
) {
    val introspector = BeanIntrospector()

    private val generators =
        listOf(
            ListPropertyGenerator(),
            BeanPropertyGenerator(),
        )

    private val fallbackGenerator = SimplePropertyGenerator()

    fun builderClassName(s: KSClassDeclaration) =
        ClassName(s.packageName.asString(), s.simpleName.asString() + "Builder")

    fun dslInterfaceName(s: KSClassDeclaration) = ClassName(s.packageName.asString(), s.simpleName.asString() + "Dsl")

    fun getPropertyTypeName(propertyType: KSTypeReference): TypeName {
        val resolvedType = propertyType.resolve()
        val generator = getGenerator(resolvedType)
        return generator.generateTypeName(this, resolvedType)
    }

    private fun getGenerator(type: KSType): PropertyGenerator = generators.find {
        it.supports(this, type)
    } ?: fallbackGenerator

    internal fun getGenerator(type: KSTypeReference): PropertyGenerator = getGenerator(type.resolve())

    fun isAnnotated(ksClassDeclaration: KSClassDeclaration): Boolean = ksClassDeclaration.annotations.any {
        annotations.contains(resolveAnnotationName(it))
    }

    private fun resolveAnnotationName(it: KSAnnotation) =
        (it.annotationType.resolve().declaration as KSClassDeclaration).qualifiedName?.asString()
}
