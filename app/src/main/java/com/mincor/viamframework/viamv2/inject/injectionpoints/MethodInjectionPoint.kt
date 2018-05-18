package com.mincor.viamframework.viamv2.inject.injectionpoints

import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector
import com.mincor.viamframework.viam.injection.InjectorError
import com.mincor.viamframework.viam.injection.injectionpoints.InjectionPoint
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass

open class MethodInjectionPoint(node: XML, injector: Injector) : InjectionPoint(node, injector) {

    /*******************************************************************************************
     * private properties *
     */
    override val methodName: String by lazy {
        val nameArgs = node.getXMLByName("arg").getXMLListByKeyValue("key", "name")
        val methodNode = node.parent!!
        this.gatherParameters(methodNode, nameArgs)
        methodNode.getValue("name")
    }
    private val parameterInjectionConfigs: MutableList<Any> = mutableListOf()
    private var requiredParameters = 0

    override fun applyInjection(target: Any, injector: Injector): Any? {
        val parameters = this.gatherParameterValues(target, injector)
        val method: Method
        try {
            val typeList = parameters[0] as List<KClass<*>>
            val typeClasses = arrayOfNulls<KClass<*>>(typeList.size)
            typeList.toTypedArray()
            method = target.javaClass.getMethod(this.methodName, typeClasses.javaClass)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            return target
        }

        try {
            method.invoke(target, *(parameters[1] as List<Any>).toTypedArray())
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return target
    }

    /**
     * Gather the parameters
     *
     * @param methodNode methodNode
     * @param nameArgs   nameArgs
     */
    protected fun gatherParameters(methodNode: XML, nameArgs: List<XML>) {
        var i = 0
        val parameters = methodNode.getXMLListByName("parameter")
        parameters.forEach { parameter ->
            var injectionName = ""
            if (nameArgs.isNotEmpty() && nameArgs[i] != null) {
                injectionName = nameArgs[i].getValue("value")
            }
            var parameterTypeName: String? = parameter.getValue("type")
            if (parameterTypeName == "*") {
                if (parameter.getValue("optional") == "false") {
                    throw InjectorError(
                            "Error in method definition of injectee. "
                                    + "Required parameters can\'t have type \""
                                    + Any::class.className() + "\".")
                } else {
                    parameterTypeName = null
                }
            }
            this.parameterInjectionConfigs.add(ParameterInjectionConfig(parameterTypeName, injectionName))
            if (parameter.getValue("optional") == "false") {
                this.requiredParameters++
            }
            i++
        }
    }

    /**
     * Gather the value of the parameter
     *
     * @param target   target
     * @param injector injector
     * @return Object[]
     */
    protected fun gatherParameterValues(target: Any, injector: Injector): Array<Any> {
        val parameters = ArrayList<Any>()
        val types = ArrayList<KClass<*>>()
        val length = this.parameterInjectionConfigs.size
        for (i in 0 until length) {
            val parameterConfig = this.parameterInjectionConfigs[i] as ParameterInjectionConfig
            val config: InjectionConfig
            try {
                config = injector.getMapping(
                        Class.forName(parameterConfig.typeName).kotlin,
                        parameterConfig.injectionName)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return arrayOf(types, parameters)
            }

            val injection = config.getResponse(injector)
            if (injection == null) {
                if (i >= this.requiredParameters) {
                    break
                }

                throw InjectorError(
                        "Injector is missing a rule to handle injection into target "
                                + target + ". Target dependency: "
                                + config.request::class.className() + ", method: "
                                + this.methodName + ", parameter: " + (i + 1))
            }
            try {
                types.add(Class.forName(parameterConfig.typeName).kotlin)
                parameters.add(injection)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }

        }
        return arrayOf(types, parameters)
    }

    /**
     * Only be gatherParameters and gatherParameterValues use
     */
    private data class ParameterInjectionConfig(var typeName: String?, var injectionName: String)
}