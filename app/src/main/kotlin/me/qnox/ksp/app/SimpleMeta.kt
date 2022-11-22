package me.qnox.ksp.app

class SimpleMeta<T>(
    name: String,
    parentStore: MutableMap<String, Any?>
) : Meta<SimpleMeta<T>>(name, parentStore) {

    operator fun invoke(v: T) {
        parentStore[name] = v
    }

}