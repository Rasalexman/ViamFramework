package com.mincor.viamframework.viam.core

import kotlin.reflect.KClass

interface IViewMap : IContextViewHolder {

    fun mapPackage(packageName: String)
    fun unmapPackage(packageName: String)
    fun hasPackage(packageName: String): Boolean

    fun mapType(type: KClass<*>)
    fun unmapType(type: KClass<*>)
    fun hasType(type: KClass<*>): Boolean

}