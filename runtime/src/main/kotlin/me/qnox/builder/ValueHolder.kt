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

    fun ifAbsent(supplier: () -> T) {
        if (!set) {
            _value = supplier()
        }
    }
}
