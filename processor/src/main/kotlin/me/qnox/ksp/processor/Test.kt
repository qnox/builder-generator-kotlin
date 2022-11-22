package me.qnox.ksp.processor

annotation class Mapping

annotation class Test(
    val mappings: Array<Mapping> = []
)
