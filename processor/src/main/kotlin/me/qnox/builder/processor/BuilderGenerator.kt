package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.BuildContext
import javax.annotation.processing.Generated

class BuilderGenerator(
    context: ProcessorContext,
    private val classDeclaration: KSClassDeclaration,
) : ClassGenerator {
    private val classBuilder: TypeSpec.Builder

    init {
        val className = context.builderClassName(classDeclaration)
        classBuilder = TypeSpec.classBuilder(className)
        classBuilder.addAnnotation(
            AnnotationSpec
                .builder(Generated::class)
                .addMember("value = [%S]", BuilderGenerator::class.asClassName())
                .build(),
        )
        classBuilder.addSuperinterface(context.dslInterfaceName(classDeclaration))
    }

    override fun type(context: ProcessorContext): TypeSpec {
        val returnType = classDeclaration.asType(emptyList()).toTypeName()
        val parameters = classDeclaration.primaryConstructor?.parameters ?: emptyList()
        val init = generateInitializer(context, parameters)
        return classBuilder
            .primaryConstructor(FunSpec.constructorBuilder().build())
            .addFunction(
                FunSpec
                    .builder("build")
                    .addParameter(
                        ParameterSpec
                            .builder(
                                "context",
                                BuildContext::class.asClassName().copy(nullable = true),
                            ).defaultValue("null")
                            .build(),
                    ).returns(returnType)
                    .addStatement("context?.preprocess(this)")
                    .addStatement("return %T(\n%L)", returnType, init)
                    .build(),
            ).also {
                context.extensions.contributeToBuilderType(context, classDeclaration, it)
            }.addOriginatingKSFile(classDeclaration.containingFile!!)
            .build()
    }

    private fun generateInitializer(context: ProcessorContext, parameters: List<KSValueParameter>): CodeBlock {
        val codeBlock = CodeBlock.builder().indent()
        parameters.forEach {
            val propertyInitializer = context.getGenerator(it.type).getConvertToObjectCode(context, it.name!!, it.type)
            codeBlock.add(
                CodeBlock.of(
                    "%L = %L,\n",
                    it.name?.asString(),
                    propertyInitializer,
                ),
            )
        }
        return codeBlock.build()
    }

    override fun addProperty(context: ProcessorContext, propertyName: String, propertyType: KSTypeReference) {
        val generator = context.getGenerator(propertyType)
        generator.contributeToBuilderClass(context, classBuilder, propertyName, propertyType)
    }
}
