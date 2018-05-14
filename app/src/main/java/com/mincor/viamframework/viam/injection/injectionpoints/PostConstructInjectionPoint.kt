package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.Injector

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class PostConstructInjectionPoint(node: XML, injector: Injector) : InjectionPoint(node, injector) {

    private lateinit var methodName: String

    /**
     * Get the PostConstructInjectionPoint.this.orderValue
     *
     * @return int
     */
    var order: Int = 0

    override fun applyInjection(target: Any, injector: Injector): Any {
        try {
            ((target as Map<String, Any>)[this.methodName] as Method).invoke(target)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return target
    }

    /*******************************************************************************************
     * protected methods *
     */
    override fun initializeInjection(node: XML) {
        val orderArg = node.getXMLListByName("arg").getXMLListByKeyValue("key", "order")
        val methodNode = node.parent
        try {
            this.order = Integer.parseInt(orderArg[0]
                    .getValue("value"))
        } catch (e: Exception) {
            this.order = 0
        }
        this.methodName = methodNode!!.getValue("name")
    }

}