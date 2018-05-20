package com.mincor.viamframework.viam.injection

import com.mincor.viamframework.viam.base.Base
import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.core.Inject
import com.mincor.viamframework.viam.core.PostConstruct
import com.mincor.viamframework.viam.injection.injectionpoints.*
import com.mincor.viamframework.viam.injection.injectionresults.InjectClassResult
import com.mincor.viamframework.viam.injection.injectionresults.InjectOtherRuleResult
import com.mincor.viamframework.viam.injection.injectionresults.InjectSingletonResult
import com.mincor.viamframework.viam.injection.injectionresults.InjectValueResult
import com.mincor.viamframework.viam.injection.injectionpoints.InjectionPoint
import com.mincor.viamframework.viamv2.inject.data.Config
import java.util.*
import kotlin.reflect.KClass

open class Injector {

    var parentInjector: Injector? = null
        set(value) {
            field = value
            this.attendedToInjectees = value?.attendedToInjectees ?: this.attendedToInjectees
        }

    private val mappings: MutableMap<String, InjectionConfig?> = HashMap()
    private var injectedDescriptions: MutableMap<String, InjectedDescription?> = WeakHashMap()
    private var attendedToInjectees: MutableMap<String, Boolean> = WeakHashMap()

    fun mapValue(whenAskedFor: KClass<*>, useValue: Any, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectValueResult(useValue)
        return config
    }

    fun mapClass(mapped: KClass<*>, instantiateClass: KClass<*>, named: String): Any {
        val config = this.getMapping(mapped, named)
        config.result = InjectClassResult(instantiateClass)
        return config
    }

    fun mapSigleton(mapped: KClass<*>, named: String): Any = this.mapSingletonOf(mapped, mapped, named)

    fun mapSingletonOf(whenAskedFor: KClass<*>, useSingletonOf: KClass<*>, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectSingletonResult(useSingletonOf)
        return config
    }

    fun mapRule(whenAskedFor: KClass<*>, useRule: InjectionConfig, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectOtherRuleResult(useRule)
        return useRule
    }

    fun getMapping(mapped: KClass<*>, named: String): InjectionConfig {
        val requestName = mapped.className()
        val mapKey = "$requestName#$named"
        val config = this.mappings[mapKey] ?: InjectionConfig(mapped, named)
        this.mappings[mapKey] = config
        return config
    }

    fun injectInto(target: Any?) {
        target?.let {

            val targetKey = "${it.hashCode()}"
            if (this.attendedToInjectees[targetKey] == true) {
                return
            }

            this.attendedToInjectees[targetKey] = true

            val targetClass = it::class
            val injectedDescription = this.injectedDescriptions[targetClass.className()] ?: this.getInjectionPoints(targetClass)
            val injectionPoints = injectedDescription.injectionPoints
            injectionPoints.forEach {
                it.applyInjection(target, this)
            }
        }
    }

    fun instantiate(clazz: KClass<*>): Any? {
        val injecteeDescription = this.injectedDescriptions[clazz.className()] ?: this.getInjectionPoints(clazz)
        val injectionPoint = injecteeDescription.ctor
        val instance = injectionPoint.applyInjection(clazz, this)
        this.injectInto(instance)
        return instance
    }

    fun unmap(clazz: KClass<*>?, named: String) {
        clazz?.let {
            val mapping = this.getConfigurationForRequest(it, named,
                    true) ?: throw InjectorError("Error while removing an injector mapping: " +
                    "No mapping defined for class ${it.className()} named \"$named\"")
            mapping.result = null
        }
    }

    fun hasMapping(clazz: KClass<*>, named: String): Boolean {
        val mapping = this.getConfigurationForRequest(clazz, named,true) ?: return false
        return mapping.hasResponse(this)
    }

    fun getInstance(clazz: KClass<*>, named: String): Any? {
        val mapping = this.getConfigurationForRequest(clazz, named,true)
        if (mapping?.hasResponse(this) == false) {
            throw InjectorError("Error while getting mapping response: "
                    + "No mapping defined for class " + clazz.className()
                    + ", named \"" + named + "\"")
        }
        return mapping?.getResponse(this)

    }

    fun createChildInjector(): Injector {
        val injector = Injector()
        injector.parentInjector = this
        return injector
    }

    internal fun getAncestorMapping(whenAskedFor: KClass<*>, named: String): InjectionConfig? {
        var parent = this.parentInjector
        while (parent != null) {
            val parentConfig = parent.getConfigurationForRequest(
                    whenAskedFor, named, false)
            if (parentConfig != null && parentConfig.hasOwnResponse()!!)
                return parentConfig
            parent = parent.parentInjector
        }
        return null
    }

    private fun getInjectionPoints(clazz: KClass<*>): InjectedDescription {
        val description = Base.describeType(clazz)

        if (clazz.java.isInterface) {
            throw InjectorError("Interfaces can't be used as instantiatable classes.")
        }
        val injectionPoints = arrayListOf<InjectionPoint>()
        val node: XML

        /*
         * get constructor injections
         */
        val ctorInjectionPoint: InjectionPoint
        node = description.getXMLByName("factory").getXMLByName("constructor")
        ctorInjectionPoint = ConstructorInjectionPoint(node, clazz, this)

        /*
         * get injection points for variables
         */
        var injectionPoint: InjectionPoint
        val childList = description.getXMLByName("factory").child
        var injectXmlList = childList.findXMLListByName("variable")
                .getXMLListByName("metadata")
                .findXMLListByKeyValue("name", Inject::class.className())
        injectXmlList.forEach {
            injectionPoint = PropertyInjectionPoint(it, null)
            injectionPoints.add(injectionPoint)
        }

        /*
         * get injection points for methods
         */
        injectXmlList = childList.findXMLListByName("method")
                .getXMLListByName("metadata")
                .findXMLListByKeyValue("name", Inject::class.className())
        injectXmlList.forEach {
            injectionPoint = MethodInjectionPoint(it, this)
            injectionPoints.add(injectionPoint)
        }

        /*
         * get post construct methods
         */
        val postConstructMethodPoints = arrayListOf<InjectionPoint>()
        injectXmlList = childList.findXMLListByName("method")
                .getXMLListByName("metadata")
                .findXMLListByKeyValue("name", PostConstruct::class.className())
        injectXmlList.forEach {
            injectionPoint = PostConstructInjectionPoint(it, this)
            postConstructMethodPoints.add(injectionPoint)
        }

        if (postConstructMethodPoints.isNotEmpty()) {
            postConstructMethodPoints.sortWith(Comparator { o1, o2 ->
                val order0 = (o1 as com.mincor.viamframework.viamv2.inject.injectionpoints.PostConstructInjectionPoint).order
                val order1 = (o2 as com.mincor.viamframework.viamv2.inject.injectionpoints.PostConstructInjectionPoint).order
                when {
                    order0 > order1 -> 1
                    order0 == order1 -> 0
                    else -> -1
                }
            })
            injectionPoints.addAll(postConstructMethodPoints)
        }

        val injectedDescription = InjectedDescription(ctorInjectionPoint, injectionPoints)
        this.injectedDescriptions[clazz.qualifiedName!!] = injectedDescription
        return injectedDescription
    }

    private fun getConfigurationForRequest(clazz: KClass<*>, named: String, traverseAncestors: Boolean): InjectionConfig? {
        val requestName = clazz.className()
        var config = this.mappings["$requestName#$named"]
        if (config == null && traverseAncestors
                && this.parentInjector != null
                && this.parentInjector!!.hasMapping(clazz, named)) {
            config = this.getAncestorMapping(clazz, named)
        }
        return config
    }

    // ---------------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------------

    private data class InjectedDescription(var ctor: InjectionPoint, var injectionPoints: List<InjectionPoint>)
}