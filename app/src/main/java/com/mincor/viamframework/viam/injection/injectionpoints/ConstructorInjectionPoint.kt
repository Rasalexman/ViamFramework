package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.core.Inject
import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class ConstructorInjectionPoint(node: XML, clazz: KClass<*>, injector: Injector) : MethodInjectionPoint(node, injector) {

    override fun applyInjection(target: Any, injector: Injector): Any? {
        val ctor = target as KClass<*>
        val parameters = this.gatherParameterValues(target, injector)
        /*
         * the only way to implement ctor injections, really
         */
        when ((parameters[1] as List<*>).size) {
            0 -> {
                try {
                    return ctor::class.java.newInstance()
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

                try {
                    val typeList = parameters[0] as List<KClass<*>>
                    val typeClasses = arrayOfNulls<KClass<*>>(typeList.size)
                    typeList.toTypedArray()
                    return ctor::class.java.getConstructor(typeClasses::class.java)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                }

            }
            else -> try {
                val typeList = parameters[0] as List<KClass<*>>
                val typeClasses = arrayOfNulls<KClass<*>>(typeList.size)
                typeList.toTypedArray()
                return ctor::class.java.getConstructor(typeClasses::class.java)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Initialize the injection
     *
     * @param node node
     */
    override fun initializeInjection(node: XML) {
        val nameArgs = node.parent?.getXMLListByNameAndKeyValue("metadata",
                "name", Inject::class.java.name)?.getXMLListByNameAndKeyValue(
                "arg", "key", "name")
        this.methodName = "constructor"
        this.gatherParameters(node, nameArgs?:kotlin.collections.arrayListOf())
    }

}