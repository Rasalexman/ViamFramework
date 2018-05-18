package com.mincor.viamframework.viamv2.inject

import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viamv2.inject.injectionresults.InjectClassResult
import com.mincor.viamframework.viamv2.inject.injectionresults.InjectValueResult
import com.mincor.viamframework.viamv2.inject.data.Config
import com.mincor.viamframework.viamv2.inject.data.Description
import java.util.*
import kotlin.reflect.KClass

class Injector : IInjector {

    override val mappings: MutableMap<String, Config> by lazy { hashMapOf<String, Config>() }
    override val injectedDescriptions: MutableMap<String, Description?> by lazy { WeakHashMap<String, Description?>() }
    override var attendedToInjectees: MutableMap<String, Boolean> = WeakHashMap()

    override var parentInjector: IInjector? = null
        set(value) {
            field = value
            this.attendedToInjectees = value?.attendedToInjectees ?: this.attendedToInjectees
        }
}

inline fun <reified T : Any> IInjector.classResult(responseType:T) {

}

inline fun <reified T : KClass<*>> IInjector.instantiate():T? {
    val clazz = T::class
    val clazzName = clazz.className()
    val injecteeDescription = this.injectedDescriptions[clazzName] ?: this.getInjectionPoints(clazz)
    val injectionPoint = injecteeDescription.ctor
    val instance = injectionPoint?.applyInjection(clazz, this)
    this.injectInto(instance)
    return instance as T
}

fun IInjector.injectInto(target:Any?){
    target?.let {

        val targetKey = "${it.hashCode()}"
        if (this.attendedToInjectees[targetKey] == true) {
            return
        }

        this.attendedToInjectees[targetKey] = true

        val targetClass = it::class
        val injectedDescription = this.injectedDescriptions[targetClass.className()] ?: getInjectionPoints(targetClass)
        val injectionPoints = injectedDescription.injectionPoints
        /*injectionPoints.forEach {
            it.applyInjection(target, this)
        }*/
    }
}


//////////------------- SYSTEM FUNCTIONS ----------/////
fun IInjector.getInjectionPoints(clazz: KClass<*>):Description {


    return Description()
}

inline fun <reified T : Any> IInjector.getMapping(mapped: T, named: String = ""):Config {
    val requestName = mapped::class.className()
    val mapKey = "$requestName#$named"
    val config = this.mappings[mapKey] ?: Config(mapped, named)
    this.mappings[mapKey] = config
    return config
}

inline fun <reified T : Any> IInjector.mapValue(whenAskedFor: T, useValue: Any, named: String = ""): Config {
    val config = this.getMapping(whenAskedFor, named)
    config.result = InjectValueResult(useValue)
    return config
}

inline fun <reified T : KClass<*>, reified I : KClass<*>> IInjector.mapClass(mapped: T, instantiateClass: I, named: String): Config {
    val config = this.getMapping(mapped, named)
    config.result = InjectClassResult(instantiateClass)
    return config
}