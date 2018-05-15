package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector
import com.mincor.viamframework.viam.injection.InjectorError
import kotlin.reflect.KClass

class PropertyInjectionPoint(node: XML, injector: Injector?) : InjectionPoint(node, null) {

    /*******************************************************************************************
     * private properties *
     */
    private var propertyName: String? = null
    private var propertyType: String? = null
    private var injectionName: String? = null

    override fun applyInjection(target: Any, injector: Injector): Any {
        val injectionConfig: InjectionConfig
        try {
            injectionConfig = injector.getMapping(Class.forName(this.propertyType).kotlin, this.injectionName?:"")
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
            target.javaClass.getField(this.propertyName!!).set(target, injection)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        return target
    }

    /*******************************************************************************************
     * protected methods *
     */
    override fun initializeInjection(node: XML) {
        this.propertyType = node.parent!!.getValue("type")
        this.propertyName = node.parent!!.getValue("name")
        this.injectionName = node.getXMLByName("arg").getValue("value")
    }

}
