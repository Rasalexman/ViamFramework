package com.mincor.viamframework.viam.core

interface IViewMap : IContentViewHolder {

    fun mapPackage(packageName: String)
    fun unmapPackage(packageName: String)
    fun hasPackage(packageName: String): Boolean

    fun <T:Class<*>>mapType(type: T)
    fun <T:Class<*>> unmapType(type: T)

}