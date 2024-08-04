package me.qnox.builder.processor.extensions.copyof

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import me.qnox.builder.processor.Extension
import me.qnox.builder.processor.ProcessorContext

class ObjectToBuilderExtension : Extension {
    override fun contributeToBuilderFile(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        fileSpec: FileSpec.Builder,
    ) {
        val bean = context.introspector.getBean(classDeclaration)
        fileSpec.addFunction(
            FunSpec
                .builder("builder")
                .receiver(classDeclaration.toClassName())
                .returns(context.builderClassName(classDeclaration))
                .beginControlFlow("return %T().also { obj ->", context.builderClassName(classDeclaration))
                .also { builder ->
                    bean.properties.forEach { property ->
                        builder.addCode(
                            context
                                .getGenerator(property.type)
                                .getConvertToBuilderCode(
                                    context,
                                    property.name,
                                    property.type,
                                    "this.${property.name}",
                                    "obj",
                                ),
                        )
                    }
                }.endControlFlow()
                .build(),
        )
    }
}
