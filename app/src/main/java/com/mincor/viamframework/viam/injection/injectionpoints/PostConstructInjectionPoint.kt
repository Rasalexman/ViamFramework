package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.Injector

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class PostConstructInjectionPoint(node: XML, injector: Injector) : InjectionPoint(node, injector) {

    private val methodName: String by lazy {
        val orderArg = node.getXMLListByName("arg").getXMLListByKeyValue("key", "order")
        val methodNode = node.parent
        try {
            this.order = orderArg[0].getValue("value").toInt()
        } catch (e: Exception) {
            this.order = 0
        }
        methodNode!!.getValue("name")
    }

    /**
     * Get the PostConstructInjectionPoint.orderValue
     *
     * @return int
     */
    var order: Int = 0

    override fun applyInjection(target: Any, injector: Injector): Any {
        try {
            val method = target.javaClass.getDeclaredMethod(this.methodName)
            method?.invoke(target)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return target
    }
}