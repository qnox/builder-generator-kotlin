package me.qnox.builder

interface ListDsl<T> {
    fun item(item: T.() -> Unit)
}

class ListBuilder<T> : ListDsl<T> {
    private val items = mutableListOf<T.() -> Unit>()

    override fun item(item: T.() -> Unit) {
        items.add(item)
    }

    fun <R> build(context: BuildContext? = null, transform: (T.() -> Unit) -> R): List<R> = items.map(transform)
}
