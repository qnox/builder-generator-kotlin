package me.qnox.builder.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import me.qnox.builder.processor.bean.BeanIntrospector

class ProcessorContext(private val annotations: Set<String>) {

    val introspector = BeanIntrospector()

    fun builderClassName(s: KSClassDeclaration) =
        ClassName(s.packageName.asString(), s.simpleName.asString() + "Builder")

    fun dslInterfaceName(s: KSClassDeclaration) =
        ClassName(s.packageName.asString(), s.simpleName.asString() + "Dsl")

    fun isAnnotated(ksClassDeclaration: KSClassDeclaration): Boolean {
        return ksClassDeclaration.annotations.any {
            annotations.contains((it.annotationType.resolve().declaration as KSClassDeclaration).qualifiedName?.asString())
        }
    }

    fun getPropertyType(propertyType: KSTypeReference): TypeName {
        return if (isAnnotated(propertyType.resolve().declaration as KSClassDeclaration)) {
            val s = propertyType.resolve().declaration as KSClassDeclaration
            builderClassName(s)
        } else {
            propertyType.toTypeName()
        }
    }
}