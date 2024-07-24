package me.qnox.builder.processor.bean

import com.google.devtools.ksp.symbol.KSTypeReference

class Property(
    val name: String,
    val type: KSTypeReference,
)
