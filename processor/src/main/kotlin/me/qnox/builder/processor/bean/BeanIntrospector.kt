package me.qnox.builder.processor.bean

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration

class BeanIntrospector {
    private val beans = mutableMapOf<KSClassDeclaration, Bean>()

    fun getBean(s: KSClassDeclaration): Bean = beans.getOrPut(s) {
        Bean(
            s
                .getDeclaredProperties()
                .filter { it.isPublic() }
                .map {
                    Property(it)
                }.toList(),
        )
    }
}
