package com.mincor.viamframework.viamv2.inject.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector
import com.mincor.viamframework.viam.injection.InjectorError
import com.mincor.viamframework.viam.injection.injectionpoints.InjectionPoint

class PropertyInjectionPoint(node: XML, injector: Injector?) : InjectionPoint(node, null) {

    override val methodName: String = ""
    private var propertyName: String = node.parent!!.getValue("name")
    private var propertyType: String = node.parent!!.getValue("type")
    private var injectionName: String = node.getXMLByName("arg").getValue("value")

    override fun applyInjection(target: Any, injector: Injector): Any {
        val injectionConfig: InjectionConfig
        try {
            val kotlinClass = if(this.propertyType == "kotlin.Any") Any::class else Class.forName(this.propertyType).kotlin
            injectionConfig = injector.getMapping(kotlinClass, this.injectionName)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return target
        }

        val injection = injectionConfig.getResponse(injector) ?: throw InjectorError(
                "Injector is missing a rule to handle injection into property \""
                        + this.propertyName + "\" of object \"" + target
                        + "\". Target dependency: \"" + this.propertyType
                        + "\", named \"" + this.injectionName + "\"")
        try {
            target.javaClass.getField(this.propertyName).set(target, injection)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        return target
    }
}
