
package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector
import com.mincor.viamframework.viam.injection.InjectorError


class PropertyInjectionPoint(node: XML, injector: Injector?) : InjectionPoint(node, null) {

    /*******************************************************************************************
     * private properties *
     */
    private var _propertyName: String? = null
    private var _propertyType: String? = null
    private var _injectionName: String? = null

    override fun applyInjection(target: Any, injector: Injector): Any {
        val injectionConfig: InjectionConfig
        try {
            injectionConfig = injector.getMapping(
                    Class.forName(this._propertyType), this._injectionName?:"")
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return target
        }

        val injection = injectionConfig.getResponse(injector) ?: throw InjectorError(
                "Injector is missing a rule to handle injection into property \""
                        + this._propertyName + "\" of object \"" + target
                        + "\". Target dependency: \"" + this._propertyType
                        + "\", named \"" + this._injectionName + "\"")
        try {
            target.javaClass.getField(this._propertyName!!)
                    .set(target, injection)
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
        this._propertyType = node.parent!!.getValue("type")
        this._propertyName = node.parent!!.getValue("name")
        this._injectionName = node.getXMLByName("arg").getValue("value")
    }

}
