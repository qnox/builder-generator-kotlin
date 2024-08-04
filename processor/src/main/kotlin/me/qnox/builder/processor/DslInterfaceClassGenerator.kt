package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import javax.annotation.processing.Generated

class DslInterfaceClassGenerator(
    context: ProcessorContext,
    private val classDeclaration: KSClassDeclaration,
) : ClassGenerator {
    private val classBuilder =
        TypeSpec.interfaceBuilder(context.dslInterfaceName(classDeclaration)).also {
            it.addAnnotation(
                AnnotationSpec
                    .builder(Generated::class)
                    .addMember("value = [%S]", BuilderGenerator::class.asClassName())
                    .build(),
            )
        }

    override fun type(context: ProcessorContext) = classBuilder
        .also {
            context.extensions.contributeToDslType(context, classDeclaration, it)
        }.build()

    override fun addProperty(context: ProcessorContext, propertyName: String, propertyType: KSTypeReference) {
        context.getGenerator(propertyType).contributeToDslClass(context, classBuilder, propertyName, propertyType)
    }
}
