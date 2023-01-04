package me.qnox.builder.processor.bean

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration

class BeanIntrospector {

    private val beans = mutableMapOf<KSClassDeclaration, Bean>()

    fun getBean(s: KSClassDeclaration): Bean {
        return beans.getOrPut(s) {
            Bean(
                s.getDeclaredProperties()
                    .map {
                        Property(it.simpleName.asString(), it.type)
                    }
                    .toList()
            )
        }
    }
}