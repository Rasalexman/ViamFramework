package com.mincor.viamframework.viam.core

interface IViewMap : IContextViewHolder {

    fun mapPackage(packageName: String)
    fun unmapPackage(packageName: String)
    fun hasPackage(packageName: String): Boolean

    fun mapType(type: Class<*>)
    fun unmapType(type: Class<*>)
    fun hasType(type: Class<*>): Boolean

}