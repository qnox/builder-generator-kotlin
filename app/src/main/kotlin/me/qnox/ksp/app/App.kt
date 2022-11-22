package me.qnox.ksp.app

@MetaAnnotation
class App(val i: Int, val sub: Sub)

@MetaAnnotation
class Sub(val s: String)

val builder = AppBuilder().apply {
    i(2)
    sub {
        s("asb")
        //sick!
        i(1)
    }
}.build()

fun main() {
    println(builder.i)
}
