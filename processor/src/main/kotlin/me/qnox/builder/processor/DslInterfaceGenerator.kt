package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.processor.bean.Property

class DslInterfaceGenerator {

    fun generateBuilderType(context: ProcessorContext, classDeclaration: KSClassDeclaration): TypeSpec {
        val className = context.dslInterfaceName(classDeclaration)
        val classBuilder = TypeSpec.interfaceBuilder(className)
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
        it: Property
    ): TypeSpec.Builder {
        return classBuilder.addFunction(
            FunSpec.builder(it.name)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("v", propertyType.toTypeName())
                .build()
        )
    }

    private fun addBuilderProperty(
        context: ProcessorContext,
        propertyType: KSTypeReference,
        classBuilder: TypeSpec.Builder,
        property: Property
    ): TypeSpec.Builder {
        val builderType = context.getPropertyType(propertyType)
        return classBuilder.addFunction(
            FunSpec.builder(property.name)
                .addModifiers(KModifier.ABSTRACT)
                .addParameter("builder", LambdaTypeName.get(builderType, returnType = UNIT))
                .build()
        )
    }
}