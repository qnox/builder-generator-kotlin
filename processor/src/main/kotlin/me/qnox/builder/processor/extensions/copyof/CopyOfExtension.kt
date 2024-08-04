package me.qnox.builder.processor.extensions.copyof

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toClassName
import me.qnox.builder.processor.Extension
import me.qnox.builder.processor.ProcessorContext

class CopyOfExtension : Extension {
    override fun contributeToDslFile(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        fileSpec: FileSpec.Builder,
    ) {
        appendCopyOf(context, fileSpec, context.builderClassName(classDeclaration), classDeclaration)
    }

    private fun appendCopyOf(
        context: ProcessorContext,
        fileSpec: FileSpec.Builder,
        builderClassName: ClassName,
        classDeclaration: KSClassDeclaration,
    ) {
        val receiverType = classDeclaration.toClassName()
        fileSpec.addFunction(
            FunSpec
                .builder("copyWith")
                .receiver(receiverType)
                .addParameter(
                    ParameterSpec
                        .builder(
                            "builder",
                            LambdaTypeName.get(builderClassName, returnType = UNIT),
                        ).build(),
                ).returns(receiverType)
                .addCode("return this.builder().apply(builder).build()", context.builderClassName(classDeclaration))
                .build(),
        )
    }
}
