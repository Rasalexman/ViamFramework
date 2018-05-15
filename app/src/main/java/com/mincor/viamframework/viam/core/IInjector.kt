package com.mincor.viamframework.viam.core

import com.mincor.viamframework.viam.injection.InjectionConfig
import kotlin.reflect.KClass

interface IInjector {

    fun mapValue(mapped: KClass<*>, valueToUse:Any, named:String = ""):Any

    fun mapClass(mapped:KClass<*>, instantiated:KClass<*>, named:String = ""):Any

    fun getMapping(mapped: KClass<*>, named: String): InjectionConfig

    fun mapSigleton(mapped:KClass<*>, named:String = ""):Any

    fun mapSingletonOf(mapped:KClass<*>, singleOf:KClass<*>, named:String = ""):Any

    fun mapRule(mapped:KClass<*>, useRule: InjectionConfig, named:String = ""):Any

    fun injectInto(target: Any?)

    fun instantiate(clazz: KClass<*>): Any?

    fun getInstance(clazz: KClass<*>, named: String = ""): Any?

    fun createChild(): IInjector

    fun unmap(clazz: KClass<*>?, named: String = "")

    fun hasMapping(clazz: KClass<*>, named: String = ""): Boolean
}