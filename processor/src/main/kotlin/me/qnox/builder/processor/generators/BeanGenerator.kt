package me.qnox.builder.processor.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import me.qnox.builder.processor.Generator
import me.qnox.builder.processor.ProcessorContext
import me.qnox.builder.processor.valueHolder

class BeanGenerator : Generator {
    override fun supports(context: ProcessorContext, classDeclaration: KSClassDeclaration): Boolean =
        context.isAnnotated(classDeclaration)

    override fun generateTypeName(context: ProcessorContext, type: KSType): TypeName {
        val s = type.declaration as KSClassDeclaration
        val arguments = type.arguments
        val builderClassName = context.builderClassName(s)
        return if (arguments.isEmpty()) {
            builderClassName
        } else {
            builderClassName.parameterizedBy(arguments.map { it.type?.let { context.getPropertyTypeName(it) } ?: STAR })
        }
    }

    override fun contributeToBuilderClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    ) {
        val nullable = propertyType.resolve().nullability == Nullability.NULLABLE
        val typeName = context.getPropertyTypeName(propertyType)
        val type = builderPropertyType(typeName, nullable)
        classBuilder.addProperty(
            PropertySpec
                .builder(propertyName, type, KModifier.PRIVATE)
                .initializer(
                    "%T(%S, %L)",
                    type,
                    propertyName,
                    nullable,
                ).build(),
        )
        classBuilder.addFunction(
            FunSpec
                .builder(propertyName)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("builder", builderLambdaType(typeName))
                .addCode(CodeBlock.of("this.%L.computeIfAbsent { %T() }?.apply(builder)", propertyName, typeName))
                .build(),
        )
        classBuilder.addFunction(
            FunSpec
                .builder(propertyName)
                .addParameter("builder", typeName)
                .addCode(CodeBlock.of("this.%L.set(builder)", propertyName))
                .build(),
        )
    }

    private fun builderPropertyType(propertyType: TypeName, nullable: Boolean) = valueHolder.parameterizedBy(
        propertyType
            .copy(nullable = nullable),
    )

    private fun builderLambdaType(builderType: TypeName) = LambdaTypeName.get(builderType, returnType = UNIT)

    override fun getConvertToObjectCode(
        context: ProcessorContext,
        propertyName: KSName,
        propertyType: KSTypeReference,
    ): CodeBlock {
        context.getPropertyTypeName(propertyType)
        val nullable = propertyType.resolve().nullability == Nullability.NULLABLE
        return CodeBlock.of(
            "this.%L.value${if (nullable) "?" else ""}.let { it.build(context) }",
            propertyName.asString(),
        )
    }

    override fun contributeToDslClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    ) {
        val typeName = context.getPropertyTypeName(propertyType)
        classBuilder.addFunction(
            FunSpec
                .builder(propertyName)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("builder", LambdaTypeName.get(typeName, returnType = UNIT))
                .build(),
        )
    }

    override fun getConvertToBuilderCode(
        context: ProcessorContext,
        propertyName: String,
        type: KSTypeReference,
        source: String,
        destination: String,
    ): CodeBlock = CodeBlock
        .builder()
        .addStatement(
            "%L?.let { %L.%L(%L.builder()) }",
            source,
            destination,
            propertyName,
            "it",
        ).build()
}
