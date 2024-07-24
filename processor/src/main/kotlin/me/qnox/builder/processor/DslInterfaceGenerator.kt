package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.processor.bean.Property
import javax.annotation.processing.Generated

class DslInterfaceGenerator {
    fun generateBuilderType(context: ProcessorContext, classDeclaration: KSClassDeclaration): TypeSpec {
        val className = context.dslInterfaceName(classDeclaration)
        val classBuilder = TypeSpec.interfaceBuilder(className)
        classBuilder.addAnnotation(
            AnnotationSpec
                .builder(Generated::class)
                .addMember("value = [%S]", BuilderGenerator::class.asClassName())
                .build(),
        )
        val bean = context.introspector.getBean(classDeclaration)
        bean.properties.map {
            val propertyType = it.type
            if (context.isAnnotated(propertyType.resolve().declaration as KSClassDeclaration)) {
                addBuilderProperty(context, propertyType, classBuilder, it)
            } else {
                addSimpleProperty(propertyType, classBuilder, it)
            }
        }
        return classBuilder.build()
    }

    private fun addSimpleProperty(
        propertyType: KSTypeReference,
        classBuilder: TypeSpec.Builder,
        it: Property,
    ): TypeSpec.Builder = classBuilder.addFunction(
        FunSpec
            .builder(it.name)
            .addModifiers(KModifier.ABSTRACT)
            .addParameter("v", propertyType.toTypeName())
            .build(),
    )

    private fun addBuilderProperty(
        context: ProcessorContext,
        propertyType: KSTypeReference,
        classBuilder: TypeSpec.Builder,
        property: Property,
    ): TypeSpec.Builder {
        val builderType = context.getPropertyType(propertyType)
        return classBuilder.addFunction(
            FunSpec
                .builder(property.name)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("builder", LambdaTypeName.get(builderType, returnType = UNIT))
                .build(),
        )
    }
}
