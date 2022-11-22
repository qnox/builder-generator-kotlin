package me.qnox.ksp.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

class TypeMapper {

    private val mapping = mutableMapOf<KSClassDeclaration, ClassName>()

    fun getMetaType(propertyType: KSTypeReference): TypeName =
        mapping[propertyType.resolve().declaration] ?: ClassName("me.qnox.ksp.app", "SimpleMeta").parameterizedBy(propertyType.toTypeName())

    fun addMapping(s: KSClassDeclaration, className: ClassName) {
        mapping[s] = className
    }

}
