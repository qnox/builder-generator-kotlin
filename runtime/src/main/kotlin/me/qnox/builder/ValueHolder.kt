package me.qnox.builder

class ValueHolder<T>(
    private val property: String,
    private val nullable: Boolean,
) {
    private var set = false
    private var _value: T? = null
    var value: T
        get() =
            if (!nullable && _value == null) {
                error("$property was not initialized")
            } else {
                _value as T
            }
        set(value) {
            _value = value
            set = true
        }

    fun set(value: T) {
        this.value = value
    }

    fun computeIfAbsent(supplier: () -> T): T {
        if (!set) {
            _value = supplier()
        }
        return _value as T
    }
}
