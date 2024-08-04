package me.qnox.builder

interface ListDsl<T> {
    fun item(item: T.() -> Unit)
}

class ListBuilder<T>(
    private val factory: () -> T,
) : ListDsl<T> {
    private val items = mutableListOf<T>()

    override fun item(item: T.() -> Unit) {
        items.add(factory().apply(item))
    }

    fun item(item: T) {
        items.add(item)
    }

    fun <R> build(context: BuildContext? = null, transform: (T) -> R): List<R> = items.map(transform)
}
