package me.qnox.builder.sample

import me.qnox.builder.ListBuilder
import me.qnox.builder.builderContext

@MetaAnnotation
class MainType(
    val i: Int,
    val subType: SubType,
    val nullableI: Int?,
    val nullableSubType: SubType?,
    val list: List<SubType>,
)

@MetaAnnotation
class SubType(
    val s: String,
)

val builder =
    MainTypeBuilder()
        .apply {
            subType {
                s("asb")
                i(1)
            }
        }.build(
            builderContext {
                preprocess(MainTypeBuilder::class) {
                    i.computeIfAbsent { 2 }
                    list.computeIfAbsent {
                        ListBuilder { SubTypeBuilder() }
                    }
                }
                preprocess(SubTypeBuilder::class) {
                    s.computeIfAbsent { "test" }
                }
            },
        )

fun main() {
    println(builder.i)
}
