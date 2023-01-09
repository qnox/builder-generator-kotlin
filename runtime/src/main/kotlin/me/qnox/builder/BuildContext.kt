package me.qnox.builder

import kotlin.reflect.KClass

class BuildContext {

    private val preprocessors = mutableMapOf<KClass<*>, (Any) -> Unit>()
    fun <T : Any> preprocess(builder: T) {
        val preprocessor = findPreprocessor(builder::class) as ((T) -> Unit)?
        preprocessor?.let { it(builder) }
    }

    private fun <T : Any> findPreprocessor(type: KClass<T>): ((T) -> Unit)? {
        return preprocessors[type]
    }

    fun <T : Any> preprocess(type: KClass<T>, preprocessor: T.() -> Unit) {
        preprocessors[type] = preprocessor as (Any) -> Unit
    }
}

fun builderContext(block: BuildContext.() -> Unit): BuildContext {
    return BuildContext().apply(block)
}
