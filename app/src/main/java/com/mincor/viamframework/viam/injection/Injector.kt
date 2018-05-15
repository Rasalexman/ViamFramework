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
import java.util.*
import kotlin.reflect.KClass

open class Injector(private val xmlMetadata: XML?) {

    var parentInjector: Injector? = null
        set(parentInjector) {
            if (field != null && parentInjector == null) {
                this.attendedToInjectees = WeakHashMap()
            }

            field = parentInjector
            if (parentInjector != null) {
                this.attendedToInjectees = parentInjector
                        .attendedToInjectees
            }

        }

    private val mappings: MutableMap<String, Any> = HashMap()

    private var injecteeDescriptions: MutableMap<String, Any?> = if (xmlMetadata != null) {
        WeakHashMap()
    } else {
        Injector.INJECTION_POINTS_CACHE
    }

    private var attendedToInjectees: MutableMap<String, Any>? = WeakHashMap()

    fun mapValue(whenAskedFor: KClass<*>, useValue: Any, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectValueResult(useValue)
        return config
    }

    fun mapClass(whenAskedFor: KClass<*>, instantiateClass: KClass<*>, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectClassResult(instantiateClass)
        return config
    }

    fun mapSigleton(mapped: KClass<*>, named: String): Any {
        return this.mapSingletonOf(mapped, mapped, named)
    }

    fun mapSingletonOf(whenAskedFor: KClass<*>,
                       useSingletonOf: KClass<*>, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectSingletonResult(useSingletonOf)
        return config
    }

    fun mapRule(whenAskedFor: KClass<*>, useRule: InjectionConfig, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.result = InjectOtherRuleResult(useRule)
        return useRule
    }

    fun getMapping(whenAskedFor: KClass<*>, named: String): InjectionConfig {
        val requestName = whenAskedFor.className()
        var config: InjectionConfig? = this.mappings["$requestName#$named"] as? InjectionConfig
        if (config == null) {
            val newConfig = InjectionConfig(whenAskedFor, named)
            this.mappings["$requestName#$named"] = newConfig
            config = newConfig
        }
        return config
    }

    fun injectInto(target: Any?) {
        target?.let { target ->

            val targetKey = "${target.hashCode()}"
            if (this.attendedToInjectees!![targetKey] != null && this.attendedToInjectees!![targetKey] as Boolean) {
                return
            }

            this.attendedToInjectees!![targetKey] = true

            val targetClass = target.javaClass.kotlin
            val injecteeDescription = if (this.injecteeDescriptions[targetClass.className()] != null)
                this.injecteeDescriptions[targetClass.className()] as InjectedDescription
            else
                this.getInjectionPoints(targetClass)
            val injectionPoints = injecteeDescription.injectionPoints
            injectionPoints.forEach {
                it.applyInjection(target, this)
            }
        }
    }

    fun instantiate(clazz: KClass<*>): Any? {
        var injecteeDescription: InjectedDescription? = this.injecteeDescriptions[clazz.className()] as? InjectedDescription
        if (injecteeDescription == null) {
            injecteeDescription = this.getInjectionPoints(clazz)
        }
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
        val injector = Injector(null)
        injector.parentInjector = this
        return injector
    }

    fun purgeInjectionPointsCache() {
        Injector.INJECTION_POINTS_CACHE = HashMap()
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
         * This is where we have to wire in the XML...
         */
        if (this.xmlMetadata != null) {
            this.createInjectionPointsFromConfigXML(description)
            this.addParentInjectionPoints(description, injectionPoints)
        }

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
                val order0 = (o1 as PostConstructInjectionPoint).order
                val order1 = (o2 as PostConstructInjectionPoint).order
                when {
                    order0 > order1 -> 1
                    order0 == order1 -> 0
                    else -> -1
                }
            })
            injectionPoints.addAll(postConstructMethodPoints)
        }

        val injectedDescription = InjectedDescription(ctorInjectionPoint, injectionPoints)
        this.injecteeDescriptions[clazz.qualifiedName!!] = injectedDescription
        return injectedDescription
    }

    private fun getConfigurationForRequest(clazz: KClass<*>, named: String, traverseAncestors: Boolean): InjectionConfig? {
        val requestName = clazz.className()
        var config: InjectionConfig? = this.mappings["$requestName#$named"] as? InjectionConfig
        if (config == null && traverseAncestors
                && this.parentInjector != null
                && this.parentInjector!!.hasMapping(clazz, named)) {
            config = this.getAncestorMapping(clazz, named)
        }
        return config
    }

    private fun createInjectionPointsFromConfigXML(description: XML) {
        val metadata = description.children().getXMLListByName("metadata")
        val nodes = metadata.getXMLListByKeyValue("name", Inject::class.className())
        nodes.addAll(metadata.getXMLListByKeyValue("name", PostConstruct::class.className()))
        for (node in nodes) {
            val pNode = node.parent
            pNode!!.children().removeAll(
                    pNode.getXMLListByNameAndKeyValue("metadata", "name",
                            Inject::class.className()))
            pNode.children().removeAll(
                    pNode.getXMLListByNameAndKeyValue("metadata", "name",
                            PostConstruct::class.className()))
        }

        val className = description.getXMLByName("factory").getValue("type")
        for (node in this.xmlMetadata!!.getXMLListByNameAndKeyValue("type",
                "name", className).children()) {
            val metaNode = XML()
            metaNode.name = "metadata"
            if (node.name == PostConstruct::class.className()) {
                metaNode.setValue("name", PostConstruct::class.className())
                if (node.getValue("order") != "") {
                    val argXml = XML()
                    argXml.name = "arg"
                    argXml.setValue("key", "order")
                    argXml.setValue("value", node.getValue("order"))
                    argXml.parent = metaNode
                    metaNode.children().add(argXml)
                }
            } else {
                metaNode.setValue("name", Inject::class.className())
                if (node.getValue("injectionname") != "") {
                    val argXml = XML()
                    argXml.name = "arg"
                    argXml.setValue("key", "name")
                    argXml.setValue("value", node.getValue("injectionname"))
                    argXml.parent = metaNode
                    metaNode.children().add(argXml)
                }
                for (arg in node.getXMLListByName("arg")) {
                    val argXml = XML()
                    argXml.name = "arg"
                    argXml.setValue("key", "name")
                    argXml.setValue("value", arg.getValue("injectionname"))
                    argXml.parent = metaNode
                    metaNode.children().add(argXml)
                }
            }
            var typeNode: XML? = null
            if (node.name == "constructor") {
                typeNode = description.getXMLByName("factory")
            } else {
                val allChildren = description.getXMLByName("factory")
                        .getAllChildren()
                for (i in allChildren.indices) {
                    if (allChildren[i].getValue("name") == node.getValue("name")) {
                        typeNode = allChildren[i]
                        break
                    }
                }
                if (typeNode == null) {
                    throw InjectorError(
                            "Error in XML configuration: Class \""
                                    + className
                                    + "\" doesn\'t contain the instance member \""
                                    + node.getValue("name") + "\"")
                }
            }
            typeNode.children().add(metaNode)
            metaNode.parent = typeNode
        }
    }

    private fun addParentInjectionPoints(description: XML,
                                         injectionPoints: MutableList<InjectionPoint>) {
        val parentClassName = description.getXMLByName("factory")
                .getXMLByName("extendsClass").getValue("type")
        if (parentClassName.isBlank())
            return

        val parentClass: KClass<*>
        try {
            parentClass = Class.forName(parentClassName).kotlin
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return
        }

        val parentKey = "${parentClass.hashCode()}"
        val parentDescription =
            this.injecteeDescriptions[parentKey] as? InjectedDescription ?:
            this.getInjectionPoints(parentClass)
        val parentInjectionPoints = parentDescription.injectionPoints
        injectionPoints.addAll(parentInjectionPoints)
    }

    // ---------------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------------

    private data class InjectedDescription(var ctor: InjectionPoint, var injectionPoints: List<InjectionPoint>)

    companion object {
        private var INJECTION_POINTS_CACHE: MutableMap<String, Any?> = WeakHashMap()
    }

}