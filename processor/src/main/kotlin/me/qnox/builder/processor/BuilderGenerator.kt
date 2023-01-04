package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.BuildContext
import me.qnox.builder.ValueHolder
import me.qnox.builder.processor.bean.Property

private val valueHolder = ValueHolder::class.asClassName()

class BuilderGenerator {

    fun generateBuilderType(context: ProcessorContext, classDeclaration: KSClassDeclaration): TypeSpec {
        val className = context.generateName(classDeclaration)
        val classBuilder = TypeSpec.classBuilder(className)
        val bean = context.introspector.getBean(classDeclaration)
        val returnType = classDeclaration.asType(emptyList()).toTypeName()
        val parameters = classDeclaration.primaryConstructor?.parameters ?: emptyList()
        bean.properties.map {
            val propertyType = it.type
            if (isBuilder(context, propertyType)) {
                addBuilderProperty(context, propertyType, classBuilder, it)
            } else {
                addSimpleProperty(propertyType, classBuilder, it)
            }
        }
        val init = generateInitializer(context, parameters)

        val internalClassName = ClassName.bestGuess("Internal")
        classBuilder.addType(
            generateInternalType(context, internalClassName, classDeclaration) {
                CodeBlock.of("this@%T.%L", className, it)
            }
        )

        return classBuilder
            .addFunction(
                FunSpec.builder("build")
                    .addParameter(
                        ParameterSpec.builder(
                            "context",
                            BuildContext::class.asClassName().copy(nullable = true)
                        )
                            .defaultValue("null")
                            .build()
                    )
                    .returns(returnType)
                    .addStatement("context?.preprocess(%T())", internalClassName)
                    .addStatement("return %T(\n%L)", returnType, init)
                    .build()
            )
            .addOriginatingKSFile(classDeclaration.containingFile!!)
            .build()
    }

    private fun generateInternalType(
        context: ProcessorContext,
        internalClassName: ClassName,
        classDeclaration: KSClassDeclaration,
        propertyAccessor: (String) -> CodeBlock
    ): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(internalClassName)
        classBuilder.addModifiers(KModifier.INNER)
        val bean = context.introspector.getBean(classDeclaration)
        bean.properties.map {
            val propertyType = it.type
            val type = if (isBuilder(context, propertyType)) {
                builderPropertyType(context, propertyType)
            } else {
                valueHolder.parameterizedBy(propertyType.toTypeName())
            }
            classBuilder.addProperty(
                PropertySpec.builder(it.name, type)
                    .getter(FunSpec.getterBuilder().addStatement("return %L", propertyAccessor(it.name)).build())
                    .build()
            )
        }

        return classBuilder
            .addOriginatingKSFile(classDeclaration.containingFile!!)
            .build()
    }

    private fun isBuilder(context: ProcessorContext, propertyType: KSTypeReference) =
        context.isAnnotated(propertyType.resolve().declaration as KSClassDeclaration)

    private fun generateInitializer(context: ProcessorContext, parameters: List<KSValueParameter>): CodeBlock {
        val codeBlock = CodeBlock.builder().indent()
        parameters.forEach {
            val propertyInitializer = if (isBuilder(context, it.type)) {
                val builderType = context.getPropertyType(it.type)
                CodeBlock.of(
                    "this.%L.value${if (it.type.toTypeName().isNullable) "?" else ""}.let { %T().apply(it).build(context) }",
                    it.name?.asString(),
                    builderType
                )
            } else {
                CodeBlock.of("this.%L.value", it.name?.asString())
            }
            codeBlock.add(
                CodeBlock.of(
                    "%L = %L,\n",
                    it.name?.asString(),
                    propertyInitializer
                )
            )
        }
        return codeBlock.build()
    }

    private fun addSimpleProperty(
        propertyType: KSTypeReference,
        classBuilder: TypeSpec.Builder,
        it: Property
    ): TypeSpec.Builder {
        val type = valueHolder.parameterizedBy(propertyType.toTypeName())
        classBuilder.addProperty(
            PropertySpec.builder(it.name, type, KModifier.PRIVATE)
                .initializer("%T(%S, %L)", type, it.name, it.type.resolve().nullability == Nullability.NULLABLE)
                .build()
        )
        return classBuilder.addFunction(
            FunSpec.builder(it.name)
                .addParameter("v", propertyType.toTypeName())
                .addCode(CodeBlock.of("this.%L.set(v)", it.name))
                .build()
        )
    }

    private fun addBuilderProperty(
        context: ProcessorContext,
        propertyType: KSTypeReference,
        classBuilder: TypeSpec.Builder,
        property: Property
    ): TypeSpec.Builder {
        val type = builderPropertyType(context, propertyType)
        classBuilder.addProperty(
            PropertySpec.builder(property.name, type, KModifier.PRIVATE)
                .initializer(
                    "%T(%S, %L)",
                    type,
                    property.name,
                    property.type.resolve().nullability == Nullability.NULLABLE
                )
                .build()
        )
        val builderType = context.getPropertyType(propertyType)
        return classBuilder.addFunction(
            FunSpec.builder(property.name)
                .addParameter("builder", builderLambdaType(builderType))
                .addCode(CodeBlock.of("this.%L.set(builder)", property.name))
                .build()
        )
    }

    private fun builderLambdaType(builderType: TypeName) =
        LambdaTypeName.get(builderType, returnType = UNIT)

    private fun builderPropertyType(
        context: ProcessorContext,
        propertyType: KSTypeReference
    ) =
        valueHolder.parameterizedBy(
            builderLambdaType(context.getPropertyType(propertyType))
                .copy(nullable = propertyType.resolve().nullability != Nullability.NOT_NULL)
        )

}