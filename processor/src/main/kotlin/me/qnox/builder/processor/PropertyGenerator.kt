package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import me.qnox.builder.ValueHolder

interface PropertyGenerator {
    fun supports(context: ProcessorContext, type: KSType): Boolean

    fun generateTypeName(context: ProcessorContext, type: KSType): TypeName

    fun contributeToBuilderClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    )

    fun contributeToDslClass(
        context: ProcessorContext,
        classBuilder: TypeSpec.Builder,
        propertyName: String,
        propertyType: KSTypeReference,
    )

    fun getConvertToObjectCode(
        context: ProcessorContext,
        propertyName: KSName,
        propertyType: KSTypeReference,
    ): CodeBlock

    fun getConvertToBuilderCode(
        context: ProcessorContext,
        propertyName: String,
        type: KSTypeReference,
        source: String,
        destination: String,
    ): CodeBlock
}

val valueHolder = ValueHolder::class.asClassName()
