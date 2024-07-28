package me.qnox.builder.processor.bean

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import kotlin.reflect.KClass

class Property(
    private val property: KSPropertyDeclaration,
) {
    @OptIn(KspExperimental::class)
    fun <T : Annotation> getAnnotationByType(annotationKClass: KClass<T>): T? =
        property.getAnnotationsByType(annotationKClass).firstOrNull()

    val name: String
        get() = property.simpleName.asString()
    val type: KSTypeReference
        get() = property.type
}
