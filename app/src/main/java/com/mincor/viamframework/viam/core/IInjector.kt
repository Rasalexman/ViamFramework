package com.mincor.viamframework.viam.core

interface IInjector {

    fun <T:Class<*>> mapValue(mapped:T, valueToUse:Any, named:String):Any

    fun <T:Class<*>, V:Any> mapClass(mapped:T, instantiated:V, named:String):Any

    fun <T:Class<*>> mapSigleton(mapped:T, named:String):Any

    fun <T:Class<*>, V:Any> mapSingletonOf(mapped:T, singleOf:V, named:String):Any

    fun <T:Class<*>> mapRule(mapped:T, useRule:Any, named:String):Any

    fun injectInto(target: Any)

    fun <T:Class<*>> instantiate(clazz: T): Any

    fun <T:Class<*>> getInstance(clazz: T, named: String): Any

    fun createChild(): IInjector

    fun <T:Class<*>> unmap(clazz: T, named: String)

    fun <T:Class<*>> hasMapping(clazz: T, named: String): Boolean
}