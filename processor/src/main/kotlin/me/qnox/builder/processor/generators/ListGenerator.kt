package me.qnox.builder.processor.generators

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getKotlinClassByName
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
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import me.qnox.builder.ListBuilder
import me.qnox.builder.ListDsl
import me.qnox.builder.processor.Generator
import me.qnox.builder.processor.ProcessorContext
import me.qnox.builder.processor.valueHolder

class ListGenerator : Generator {
    @OptIn(KspExperimental::class)
    override fun supports(context: ProcessorContext, classDeclaration: KSClassDeclaration): Boolean =
        context.resolver.getKotlinClassByName(List::class.qualifiedName!!) == classDeclaration

    override fun generateTypeName(context: ProcessorContext, type: KSType): TypeName =
        ListDsl::class.asTypeName().parameterizedBy(getItemTypeName(context, type))

    override fun contributeToBuilderClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    ) {
        val nullable = propertyType.resolve().nullability == Nullability.NULLABLE
        val type = context.getPropertyTypeName(propertyType)
        val builderType = getBuilderType(context, propertyType)
        val builderPropertyType = valueHolder.parameterizedBy(builderType)
        classBuilder.addProperty(
            PropertySpec
                .builder(propertyName, builderPropertyType, KModifier.PUBLIC)
                .initializer(
                    "%T(%S, %L)",
                    builderPropertyType,
                    propertyName,
                    nullable,
                ).build(),
        )
        classBuilder.addFunction(
            FunSpec
                .builder(propertyName)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter("builder", LambdaTypeName.get(type, returnType = UNIT))
                .addCode(
                    CodeBlock.of(
                        "this.%L.computeIfAbsent { %T { %T() } }?.apply(builder)",
                        propertyName,
                        builderType,
                        getItemBuilderName(context, propertyType.resolve()),
                    ),
                ).build(),
        )
        classBuilder.addFunction(
            FunSpec
                .builder(propertyName)
                .addParameter("builder", builderType)
                .addCode(CodeBlock.of("this.%L.set(builder)", propertyName))
                .build(),
        )
    }

    override fun contributeToDslClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    ) {
        val type = context.getPropertyTypeName(propertyType)
        classBuilder.addFunction(
            FunSpec
                .builder(propertyName)
                .addParameter("builder", LambdaTypeName.get(type, returnType = UNIT))
                .build(),
        )
    }

    override fun getConvertToObjectCode(
        context: ProcessorContext,
        propertyName: KSName,
        propertyType: KSTypeReference,
    ): CodeBlock {
        val nullable = propertyType.resolve().nullability == Nullability.NULLABLE
        return CodeBlock.of(
            "this.%L.value${if (nullable) "?" else ""}.let { it.build(context) { i -> i.build(context) } }",
            propertyName.asString(),
        )
    }

    private fun getBuilderType(context: ProcessorContext, propertyType: KSTypeReference) =
        ListBuilder::class.asClassName().parameterizedBy(getItemTypeName(context, propertyType.resolve()))

    private fun getItemTypeName(context: ProcessorContext, type: KSType): TypeName {
        val param = getItemType(context, type)
        val itemType = context.getPropertyTypeName(param)
        return itemType
    }

    private fun getItemBuilderName(context: ProcessorContext, type: KSType): TypeName {
        val param = getItemType(context, type)
        val itemType = context.builderClassName(param.resolve().declaration as KSClassDeclaration)
        return itemType
    }

    @OptIn(KspExperimental::class)
    private fun getItemType(context: ProcessorContext, type: KSType): KSTypeReference {
        val param =
            type.arguments[0].type ?: context.resolver.createKSTypeReferenceFromKSType(
                context.resolver.getKotlinClassByName(Any::class.qualifiedName!!)!!.asType(listOf()),
            )
        return param
    }

    override fun getConvertToBuilderCode(
        context: ProcessorContext,
        propertyName: String,
        type: KSTypeReference,
        source: String,
        destination: String,
    ): CodeBlock = CodeBlock.of(
        "%L.%L(%T { %T() }.also { builder -> %L.forEach{ builder.item(it.builder()) } })\n",
        destination,
        propertyName,
        getBuilderType(context, type),
        getItemBuilderName(context, type.resolve()),
        source,
    )
}
