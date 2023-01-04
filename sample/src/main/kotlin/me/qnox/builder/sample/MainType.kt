package me.qnox.builder.sample

import me.qnox.builder.builderContext

@MetaAnnotation
class MainType(val i: Int, val subType: SubType, val nullableI: Int?, val nullableSubType: SubType?)

@MetaAnnotation
class SubType(val s: String)

val builder = MainTypeBuilder().apply {
    subType {
        s("asb")
        i(1)
    }
}
    .build(builderContext {
        preprocess(MainTypeBuilder.Internal::class) {
            i.ifAbsent { 2 }
        }
        preprocess(SubTypeBuilder.Internal::class) {
            s.ifAbsent { "test" }
        }
    })

fun main() {
    println(builder.i)
}
