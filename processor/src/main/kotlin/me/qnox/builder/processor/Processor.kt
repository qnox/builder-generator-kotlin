package me.qnox.builder.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.writeTo
import me.qnox.builder.Builder
import me.qnox.builder.processor.bean.Bean

class Processor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private val annotationName = Builder::class.qualifiedName ?: error("No qualified name for annotation class")

    private val annotations = mutableSetOf(annotationName)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        updateAnnotations(resolver)
        val context = ProcessorContext(resolver, annotations, Extensions())
        return getAnnotatedClasses(resolver, annotations)
            .filter { it.classKind == ClassKind.CLASS }
            .partition { it.validate() }
            .let { (valid, defer) ->
                valid.forEach {
                    val bean = context.introspector.getBean(it)
                    writeDsl(context, it, bean)
                    writeBuilder(context, it, bean)
                }
                defer
            }
    }

    private fun generateType(context: ProcessorContext, bean: Bean, classGenerator: ClassGenerator): TypeSpec {
        bean.properties.map { property ->
            val propertyType = property.type
            val propertyName = property.name
            classGenerator.addProperty(context, propertyName, propertyType)
        }
        return classGenerator.type(context)
    }

    private fun writeDsl(context: ProcessorContext, classDeclaration: KSClassDeclaration, bean: Bean) {
        val builderClassName = context.dslInterfaceName(classDeclaration)
        val fileName = builderClassName.simpleName
        val file =
            FileSpec
                .builder(classDeclaration.packageName.asString(), fileName)
                .addType(
                    generateType(context, bean, DslInterfaceClassGenerator(context, classDeclaration)),
                ).also {
                    context.extensions.contributeToDslFile(context, classDeclaration, it)
                }.build()
        file.writeTo(codeGenerator, file.kspDependencies(false))
    }

    private fun writeBuilder(context: ProcessorContext, classDeclaration: KSClassDeclaration, bean: Bean) {
        val builderClassName = context.builderClassName(classDeclaration)
        val fileName = builderClassName.simpleName
        val file =
            FileSpec
                .builder(classDeclaration.packageName.asString(), fileName)
                .addType(
                    generateType(context, bean, BuilderGenerator(context, classDeclaration)),
                ).also {
                    context.extensions.contributeToBuilderFile(context, classDeclaration, it)
                }.build()
        file.writeTo(codeGenerator, file.kspDependencies(false))
    }

    private fun updateAnnotations(resolver: Resolver) {
        annotations.addAll(annotations.flatMap { metaAnnotations(resolver, it) })
    }

    private fun metaAnnotations(resolver: Resolver, annotationName: String): Set<String> {
        val metaAnnotations =
            resolver
                .getSymbolsWithAnnotation(annotationName)
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ANNOTATION_CLASS }
                .mapNotNull { it.qualifiedName?.asString() }
                .flatMap { metaAnnotations(resolver, it) }
                .toSet()
        return setOf(annotationName) + metaAnnotations
    }

    private fun getAnnotatedClasses(resolver: Resolver, annotationNames: Set<String>): Sequence<KSClassDeclaration> =
        annotationNames
            .asSequence()
            .flatMap { resolver.getSymbolsWithAnnotation(it) }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }
}
