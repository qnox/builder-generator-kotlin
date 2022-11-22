package me.qnox.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class ProcessorContext {

    val introspector = BeanIntrospector()

    val typeMapper = TypeMapper()

}

/**
 * 2 generation strategy: SELF and gen
 */

class Processor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = ProcessorContext()
        logger.info("TBD!!!")
        val symbols = resolver.getSymbolsWithAnnotation("me.qnox.ksp.processor.Test")
        val classes = (symbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ANNOTATION_CLASS }
            .mapNotNull { it.qualifiedName?.asString() }
            .flatMap {
                logger.info(it)
                resolver.getSymbolsWithAnnotation(it)
            } + symbols)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }
        for (s in classes) {
            context.typeMapper.addMapping(s, className(s))
        }
        for (s in classes) {
            if (s is KSClassDeclaration && s.validate() && s.classKind == ClassKind.CLASS) {
                val greeterClass = className(s)
                val file = FileSpec.builder(s.packageName.asString(), greeterClass.simpleName)
                    .addType(
                        generateBuilderType(greeterClass, s, context)
                    )
                    .addFunction(
                        FunSpec.builder("main")
                            .addParameter("args", String::class, KModifier.VARARG)
                            .addStatement("%T().build()", greeterClass)
                            .build()
                    )
                    .build()

                file.writeTo(codeGenerator, file.kspDependencies(false))
                logger.info(s.qualifiedName?.asString() ?: "null")
            }
        }
        return emptyList()
    }

    private val base = ClassName("me.qnox.ksp.app", "Meta")

    private fun className(s: KSClassDeclaration) =
        ClassName(s.packageName.asString(), s.simpleName.asString() + "Builder")

    private fun generateBuilderType(
        greeterClass: ClassName,
        s: KSClassDeclaration,
        context: ProcessorContext
    ): TypeSpec {
        val bean = context.introspector.getBean(s)
        val returnType = s.asType(emptyList()).toTypeName()
        val storeProperty =
            PropertySpec.builder(
                "_store",
                MUTABLE_MAP.parameterizedBy(STRING, ANY.copy(nullable = true)),
                KModifier.PRIVATE
            )
                .initializer("mutableMapOf()")
                .build()
        val parameters = s.primaryConstructor?.parameters ?: emptyList()
        val init = parameters.joinToString {
            CodeBlock.of(
                "%L = %N[%S] as %T",
                it.name?.asString(),
                storeProperty,
                it.name?.asString(),
                it.type.toTypeName()
            ).toString()
        }
        return TypeSpec.classBuilder(greeterClass)
            .superclass(base.parameterizedBy(greeterClass))
            .addProperty(storeProperty)
            .addProperties(
                bean.properties.map {
                    val propertyType = it.type
                    val type = context.typeMapper.getMetaType(propertyType)
                    PropertySpec.builder(
                        it.name,
                        type
                    )
                        .initializer("%T(%S, %N)", type, it.name, storeProperty)
                        .build()
                }.toList()
            )
            .addFunction(
                FunSpec.builder("build")
                    .returns(returnType)
                    .addStatement("return %T(%L)", returnType, init)
                    .build()
            )
            .addOriginatingKSFile(s.containingFile!!)
            .build()
    }
}