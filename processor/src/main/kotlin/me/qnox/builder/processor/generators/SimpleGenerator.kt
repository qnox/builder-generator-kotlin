package me.qnox.builder.processor.generators

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.processor.Generator
import me.qnox.builder.processor.ProcessorContext
import me.qnox.builder.processor.valueHolder

class SimpleGenerator : Generator {
    override fun supports(context: ProcessorContext, classDeclaration: KSClassDeclaration): Boolean = true

    override fun generateTypeName(context: ProcessorContext, type: KSType): TypeName = type.toTypeName()

    override fun contributeToBuilderClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    ) {
        val nullable = propertyType.resolve().nullability == Nullability.NULLABLE
        val typeName = context.getPropertyTypeName(propertyType)
        val type = valueHolder.parameterizedBy(typeName)
        classBuilder.addProperty(
            PropertySpec
                .builder(propertyName, type)
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
                .addParameter("v", typeName)
                .addCode(CodeBlock.of("this.%L.set(v)", propertyName))
                .build(),
        )
    }

    override fun generateBuildCode(
        context: ProcessorContext,
        propertyName: KSName,
        propertyType: KSTypeReference,
    ): CodeBlock = CodeBlock.of("this.%L.value", propertyName.asString())

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
                .addParameter("v", typeName)
                .build(),
        )
    }
}
