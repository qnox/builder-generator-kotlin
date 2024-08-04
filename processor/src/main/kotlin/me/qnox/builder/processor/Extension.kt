package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

interface Extension {
    fun contributeToDslType(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        typeSpecBuilder: TypeSpec.Builder,
    ) {}

    fun contributeToDslFile(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        fileSpec: FileSpec.Builder,
    ) {}

    fun contributeToBuilderType(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        typeSpec: TypeSpec.Builder,
    ) {}

    fun contributeToBuilderFile(
        context: ProcessorContext,
        classDeclaration: KSClassDeclaration,
        fileSpec: FileSpec.Builder,
    ) {}
}
