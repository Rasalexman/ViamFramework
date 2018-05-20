package com.mincor.viamframework.viamv2.inject

import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viam.injection.InjectorError
import com.mincor.viamframework.viamv2.inject.data.Config
import com.mincor.viamframework.viamv2.inject.data.Description
import com.mincor.viamframework.viamv2.inject.injectionresults.InjectClassResult
import com.mincor.viamframework.viamv2.inject.injectionresults.InjectSingletonResult
import com.mincor.viamframework.viamv2.inject.injectionresults.InjectValueResult
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class Injector : IInjector {

    val mappings: MutableMap<String, Config?> by lazy { hashMapOf<String, Config?>() }
    val injectedDescriptions: MutableMap<String, Description?> by lazy { WeakHashMap<String, Description?>() }
    var attendedToInjectees: MutableMap<String, Boolean> = WeakHashMap()

    var parentInjector: Injector? = null
        set(value) {
            field = value
            this.attendedToInjectees = value?.attendedToInjectees ?: this.attendedToInjectees
        }
}

inline fun <reified T : Any> IInjector.inject():InjectorReadProperty<T> {
return InjectorReadProperty()
}

class InjectorReadProperty<T: Any> : ReadOnlyProperty<IInjector?, T> {
    private var value: T? = null

    override fun getValue(thisRef: IInjector?, property: KProperty<*>): T {

        //value = thisRef?.instantiate<T::class>()
        return value ?: throw Error("NOT IMPLEMENTED")
    }
}

fun Injector.instantiate(clazz: KClass<*>): Any? {
    val clazzName = clazz.className()
    val injecteeDescription = this.injectedDescriptions[clazzName] ?: this.getInjectionPoints(clazz)
    val injectionPoint = injecteeDescription.ctor
    val instance = injectionPoint?.applyInjection(clazz, this)
    this.injectInto(instance)
    return instance
}

fun Injector.injectInto(target: Any?) {
    target?.let {

        val targetKey = "${it.hashCode()}"
        if (this.attendedToInjectees[targetKey] == true) {
            return
        }

        this.attendedToInjectees[targetKey] = true

        val targetClass = it::class
        val injectedDescription = this.injectedDescriptions[targetClass.className()]
                ?: getInjectionPoints(targetClass)
        val injectionPoints = injectedDescription.injectionPoints
        injectionPoints?.forEach {
            it.applyInjection(target, this)
        }
    }
}


//////////------------- SYSTEM FUNCTIONS ----------/////
fun IInjector.getInjectionPoints(clazz: KClass<*>): Description {


    return Description()
}

inline fun <reified T : Any> Injector.getMapping(named: String = ""): Config {
    val clazz = T::class
    val requestName = clazz.className()
    val mapKey = "$requestName#$named"
    val config = this.mappings[mapKey] ?: Config(clazz, named)
    this.mappings[mapKey] = config
    return config
}

inline fun <reified T : Any> Injector.mapValue(useValue: Any, named: String = ""): Config {
    val config = getMapping<T>(named)
    config.result = InjectValueResult(useValue)
    return config
}

inline fun <reified T : Any, reified I : Any> Injector.mapClass(named: String = ""): Config {
    val config = this.getMapping<T>(named)
    config.result = InjectClassResult(I::class)
    return config
}

inline fun <reified T : Any, reified I : Any> Injector.mapSingleton(named: String): Any? {
    val config = this.getMapping<T>(named)
    config.result = InjectSingletonResult(I::class)
    return config
}

inline fun <reified T : Any> Injector.unmap(named: String = "") {
    val clazz = T::class
    val mapping = this.getConfigurationForRequest(clazz, named,
            true) ?: throw InjectorError("Error while removing an injector mapping: " +
            "No mapping defined for class ${clazz.className()} named \"$named\"")
    mapping.result = null

}

fun Injector.hasMapping(clazz: KClass<*>, named: String): Boolean {
    val mapping = this.getConfigurationForRequest(clazz, named, true) ?: return false
    return mapping.hasResponse(this)
}

fun Injector.getConfigurationForRequest(clazz: KClass<*>, named: String, traverseAncestors: Boolean): Config? {
    val requestName = clazz.className()
    var config = this.mappings["$requestName#$named"]
    if (config == null && traverseAncestors
            && this.parentInjector != null
            && this.parentInjector!!.hasMapping(clazz, named)) {
        config = this.getAncestorMapping(clazz, named)
    }
    return config
}

fun Injector.getAncestorMapping(whenAskedFor: Any, named: String): Config? {
    var parent = this.parentInjector
    while (parent != null) {
        val parentConfig = parent.getConfigurationForRequest(whenAskedFor::class, named, false)
        if (parentConfig?.result != null)
            return parentConfig
        parent = parent.parentInjector
    }
    return null
}