package com.mincor.viamframework.viam.core

import kotlin.reflect.KClass

interface IReflector {

    fun classExtendsOrImplements(classOrClassName: Any, superclass: KClass<*>): Boolean
    fun getClass(value: Any): KClass<*>
    fun getFullyQualifiedClassName(value: Any, replaceColons: Boolean): String
}