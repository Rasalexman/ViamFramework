package com.mincor.viamframework.viam.core

interface IReflector {

    fun <T:Class<*>> classExtendsOrImplements(classOrClassName: Any,
                                 superclass: T): Boolean

    fun getClass(value: Any?): Class<*>

    fun getFullyQualifiedClassName(value: Any, replaceColons: Boolean): String
}