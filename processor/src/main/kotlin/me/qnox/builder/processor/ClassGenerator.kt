package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.TypeSpec

interface ClassGenerator {
    fun type(): TypeSpec

    fun addProperty(context: ProcessorContext, propertyName: String, propertyType: KSTypeReference)
}
