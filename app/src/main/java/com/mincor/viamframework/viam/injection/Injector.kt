package com.mincor.viamframework.viam.injection

import com.mincor.viamframework.viam.base.Base
import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.core.Inject
import com.mincor.viamframework.viam.core.PostConstruct
import com.mincor.viamframework.viam.injection.injectionpoints.*
import com.mincor.viamframework.viam.injection.injectionresults.InjectClassResult
import com.mincor.viamframework.viam.injection.injectionresults.InjectOtherRuleResult
import com.mincor.viamframework.viam.injection.injectionresults.InjectSingletonResult
import com.mincor.viamframework.viam.injection.injectionresults.InjectValueResult
import java.util.*

open class Injector(private val m_xmlMetadata: XML?) {

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

    private val m_mappings: MutableMap<String, Any> = HashMap()

    private var m_injecteeDescriptions: MutableMap<String, Any?> = if (m_xmlMetadata != null) {
        WeakHashMap()
    } else {
        Injector.INJECTION_POINTS_CACHE
    }

    internal var attendedToInjectees: MutableMap<String, Any>? = WeakHashMap()
        private set

    fun mapValue(whenAskedFor: Class<*>, useValue: Any, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.setResult(InjectValueResult(useValue))
        return config
    }

    fun mapClass(whenAskedFor: Class<*>, instantiateClass: Class<*>, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.setResult(InjectClassResult(instantiateClass))
        return config
    }

    fun mapSigleton(mapped: Class<*>, named: String): Any {
        return this.mapSingletonOf(mapped, mapped, named)
    }

    fun mapSingletonOf(whenAskedFor: Class<*>,
                       useSingletonOf: Class<*>, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.setResult(InjectSingletonResult(useSingletonOf))
        return config
    }

    fun mapRule(whenAskedFor: Class<*>, useRule: Any, named: String): Any {
        val config = this.getMapping(whenAskedFor, named)
        config.setResult(InjectOtherRuleResult(useRule as InjectionConfig))
        return useRule
    }

    fun getMapping(whenAskedFor: Class<*>, named: String): InjectionConfig {
        val requestName = whenAskedFor.name
        var config: InjectionConfig? = this.m_mappings["$requestName#$named"] as? InjectionConfig
        if (config == null) {
            val newConfig = InjectionConfig(whenAskedFor, named)
            this.m_mappings["$requestName#$named"] = newConfig
            config = newConfig
        }
        return config
    }

    fun injectInto(target: Any?) {
        target?.let {
            if (this.attendedToInjectees!![target.hashCode().toString() + ""] != null && this.attendedToInjectees!![target.hashCode().toString() + ""] as Boolean) {
                return
            }

            this.attendedToInjectees!![target.hashCode().toString() + ""] = true

            val targetClass = target.javaClass
            val injecteeDescription = if (this.m_injecteeDescriptions[targetClass.name] != null)
                this.m_injecteeDescriptions[targetClass.name] as InjecteeDescription
            else
                this.getInjectionPoints(targetClass)
            val injectionPoints = injecteeDescription.injectionPoints
            injectionPoints.forEach {
                it.applyInjection(target, this)
            }
        }
    }

    fun instantiate(clazz: Class<*>): Any? {
        var injecteeDescription: InjecteeDescription? = this.m_injecteeDescriptions[clazz.name] as? InjecteeDescription
        if (injecteeDescription == null) {
            injecteeDescription = this.getInjectionPoints(clazz)
        }
        val injectionPoint = injecteeDescription.ctor
        val instance = injectionPoint.applyInjection(clazz, this)
        this.injectInto(instance)
        return instance
    }

    fun unmap(clazz: Class<*>?, named: String) {
        clazz?.let {
            val mapping = this.getConfigurationForRequest(it, named,
                    true) ?: throw InjectorError("Error while removing an injector mapping: " +
                    "No mapping defined for class ${clazz.name} named \"$named\"")
            mapping.setResult(null)
        }
    }

    fun hasMapping(clazz: Class<*>, named: String): Boolean {
        val mapping = this.getConfigurationForRequest(clazz, named,
                true) ?: return false
        return mapping.hasResponse(this)
    }

    fun getInstance(clazz: Class<*>, named: String): Any? {
        val mapping = this.getConfigurationForRequest(clazz, named,
                true)
        if (mapping == null || (!mapping.hasResponse(this))) {
            throw InjectorError("Error while getting mapping response: "
                    + "No mapping defined for class " + clazz.name
                    + ", named \"" + named + "\"")
        }
        return mapping.getResponse(this)

    }

    fun createChildInjector(): Injector {
        val injector = Injector(null)
        injector.parentInjector = this
        return injector
    }

    fun purgeInjectionPointsCache() {
        Injector.INJECTION_POINTS_CACHE = HashMap()
    }

    internal fun getAncestorMapping(whenAskedFor: Class<*>, named: String): InjectionConfig? {
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

    private fun getInjectionPoints(clazz: Class<*>): InjecteeDescription {
        val description = Base.describeType(clazz)
        if (description.name !== "Object" && (description.getXMLByName("factory") == null || description
                        .getXMLByName("factory").getXMLByName("extendsClass") == null)) {
            throw InjectorError(
                    "Interfaces can't be used as instantiatable classes.")
        }
        val injectionPoints = arrayListOf<InjectionPoint>()
        val node: XML
        /*
         * This is where we have to wire in the XML...
         */
        if (this.m_xmlMetadata != null) {
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
                .findXMLListByKeyValue("name", Inject::class.java.name)
        injectXmlList.forEach {
            injectionPoint = PropertyInjectionPoint(it, null)
            injectionPoints.add(injectionPoint)
        }

        /*
         * get injection points for methods
         */
        injectXmlList = childList.findXMLListByName("method")
                .getXMLListByName("metadata")
                .findXMLListByKeyValue("name", Inject::class.java.name)
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
                .findXMLListByKeyValue("name", PostConstruct::class.java.name)
        for (nodeXml in injectXmlList) {
            injectionPoint = PostConstructInjectionPoint(nodeXml, this)
            postConstructMethodPoints.add(injectionPoint)
        }

        if (postConstructMethodPoints.size > 0) {
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

        val injecteeDescription = InjecteeDescription(ctorInjectionPoint, injectionPoints)
        this.m_injecteeDescriptions[clazz.name] = injecteeDescription
        return injecteeDescription

    }

    private fun getConfigurationForRequest(clazz: Class<*>, named: String, traverseAncestors: Boolean): InjectionConfig? {
        val requestName = clazz.name
        var config: InjectionConfig? = this.m_mappings["$requestName#$named"] as? InjectionConfig
        if (config == null && traverseAncestors
                && this.parentInjector != null
                && this.parentInjector!!.hasMapping(clazz, named)) {
            config = this.getAncestorMapping(clazz, named)
        }
        return config
    }

    private fun createInjectionPointsFromConfigXML(description: XML) {
        val metadata = description.children().getXMLListByName("metadata")
        val nodes = metadata.getXMLListByKeyValue("name",
                Inject::class.java.name)
        nodes.addAll(metadata.getXMLListByKeyValue("name",
                PostConstruct::class.java.name))
        for (node in nodes) {
            val pNode = node.parent
            pNode!!.children().removeAll(
                    pNode.getXMLListByNameAndKeyValue("metadata", "name",
                            Inject::class.java.name))
            pNode.children().removeAll(
                    pNode.getXMLListByNameAndKeyValue("metadata", "name",
                            PostConstruct::class.java.name))
        }

        val className = description.getXMLByName("factory").getValue("type")
        for (node in this.m_xmlMetadata!!.getXMLListByNameAndKeyValue("type",
                "name", className).children()) {
            val metaNode = XML()
            metaNode.name = "metadata"
            if (node.name == PostConstruct::class.java.name) {
                metaNode.setValue("name", PostConstruct::class.java.name)
                if (node.getValue("order") != "") {
                    val argXml = XML()
                    argXml.name = "arg"
                    argXml.setValue("key", "order")
                    argXml.setValue("value", node.getValue("order"))
                    argXml.parent = metaNode
                    metaNode.children().add(argXml)
                }
            } else {
                metaNode.setValue("name", Inject::class.java.name)
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
        if (parentClassName == null || parentClassName == "")
            return

        val parentClass: Class<*>
        try {
            parentClass = Class.forName(parentClassName)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return
        }

        val parentKey = "${parentClass.hashCode()}"
        val parentDescription = if (this.m_injecteeDescriptions[parentKey] != null)
            this.m_injecteeDescriptions[parentKey] as InjecteeDescription
        else
            this.getInjectionPoints(parentClass)
        val parentInjectionPoints = parentDescription.injectionPoints
        injectionPoints.addAll(parentInjectionPoints)
    }

    // ---------------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------------

    private inner class InjecteeDescription(var ctor: InjectionPoint,
                                            var injectionPoints: List<InjectionPoint>)

    companion object {
        private var INJECTION_POINTS_CACHE: MutableMap<String, Any?> = WeakHashMap()
    }

}