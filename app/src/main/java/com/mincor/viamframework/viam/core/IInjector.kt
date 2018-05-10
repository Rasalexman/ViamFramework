package com.mincor.viamframework.viam.core

interface IInjector {

    fun mapValue(mapped:Class<*>, valueToUse:Any, named:String):Any

    fun mapClass(mapped:Class<*>, instantiated:Class<*>, named:String):Any

    fun mapSigleton(mapped:Class<*>, named:String):Any

    fun mapSingletonOf(mapped:Class<*>, singleOf:Class<*>, named:String):Any

    fun mapRule(mapped:Class<*>, useRule:Any, named:String):Any

    fun injectInto(target: Any?)

    fun instantiate(clazz: Class<*>): Any?

    fun getInstance(clazz: Class<*>, named: String): Any?

    fun createChild(): IInjector

    fun unmap(clazz: Class<*>?, named: String)

    fun hasMapping(clazz: Class<*>, named: String): Boolean
}