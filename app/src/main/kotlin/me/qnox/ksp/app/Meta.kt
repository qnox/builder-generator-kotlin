package me.qnox.ksp.app

open class Meta<SELF : Meta<SELF>>(
    protected val name: String,
    protected val parentStore: MutableMap<String, Any?>
) {

    operator fun invoke(v: SELF) {
        parentStore[name] = v
    }

    operator fun invoke(block: SELF.() -> Unit) {
        block(this as SELF)
    }

}

