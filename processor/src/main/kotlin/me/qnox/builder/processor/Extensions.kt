package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import me.qnox.builder.processor.extensions.copyof.CopyOfExtension
import me.qnox.builder.processor.extensions.copyof.ObjectToBuilderExtension

class Extensions {
    private val extensions =
        listOf(
            ObjectToBuilderExtension(),
            CopyOfExtension(),
        )

    fun contributeToDslType(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        typeSpec: TypeSpec.Builder,
    ) {
        extensions.forEach {
            it.contributeToDslType(context, classDeclaration, typeSpec)
        }
    }

    fun contributeToDslFile(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        fileSpec: FileSpec.Builder,
    ) {
        extensions.forEach {
            it.contributeToDslFile(context, classDeclaration, fileSpec)
        }
    }

    fun contributeToBuilderType(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        typeSpec: TypeSpec.Builder,
    ) {
        extensions.forEach {
            it.contributeToBuilderType(context, classDeclaration, typeSpec)
        }
    }

    fun contributeToBuilderFile(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        fileSpec: FileSpec.Builder,
    ) {
        extensions.forEach {
            it.contributeToBuilderFile(context, classDeclaration, fileSpec)
        }
    }
}
