package com.mincor.viamframework.viam.core

interface IReflector {

    fun classExtendsOrImplements(classOrClassName: Any, superclass: Class<*>): Boolean
    fun getClass(value: Any): Class<*>
    fun getFullyQualifiedClassName(value: Any, replaceColons: Boolean): String
}