package me.qnox.builder.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.writeTo
import me.qnox.builder.Builder

class Processor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    private val annotationName = Builder::class.qualifiedName ?: error("No qualified name for annotation class")

    private val annotations = mutableSetOf(annotationName)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        updateAnnotations(resolver)
        val context = ProcessorContext(annotations)
        val builderGenerator = BuilderGenerator()
        return getAnnotatedClasses(resolver, annotations)
            .filter { it.classKind == ClassKind.CLASS }
            .partition { it.validate() }
            .let { (valid, defer) ->
                valid.forEach {
                    val builderClassName = context.generateName(it)
                    val fileName = builderClassName.simpleName
                    val file = FileSpec.builder(it.packageName.asString(), fileName)
                        .addType(builderGenerator.generateBuilderType(context, it))
                        .build()
                    file.writeTo(codeGenerator, file.kspDependencies(false))
                }
                defer
            }
    }

    private fun updateAnnotations(resolver: Resolver) {
        annotations.addAll(annotations.flatMap { metaAnnotations(resolver, it) })
    }

    private fun metaAnnotations(resolver: Resolver, annotationName: String): Set<String> {
        val metaAnnotations = resolver.getSymbolsWithAnnotation(annotationName)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ANNOTATION_CLASS }
            .mapNotNull { it.qualifiedName?.asString() }
            .flatMap { metaAnnotations(resolver, it) }
            .toSet()
        return setOf(annotationName) + metaAnnotations
    }

    private fun getAnnotatedClasses(resolver: Resolver, annotationNames: Set<String>): Sequence<KSClassDeclaration> {
        return annotationNames
            .asSequence()
            .flatMap { resolver.getSymbolsWithAnnotation(it) }
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }
    }

}