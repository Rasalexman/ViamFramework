package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viam.base.ext.construct
import com.mincor.viamframework.viam.base.ext.factoryCreator
import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.core.Inject
import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class ConstructorInjectionPoint(node: XML, clazz: KClass<*>, injector: Injector) : MethodInjectionPoint(node, injector) {

    override val methodName: String by lazy {
        val nameArgs = node.parent?.getXMLListByNameAndKeyValue("metadata",
                "name", Inject::class.className())?.getXMLListByNameAndKeyValue(
                "arg", "key", "name")
        this.gatherParameters(node, nameArgs?:arrayListOf())
        "constructor"
    }

    override fun applyInjection(target: Any, injector: Injector): Any? {
        if(target !is KClass<*>){
            return null
        }
        val ctor = target as? KClass<*>
        val parameters = this.gatherParameterValues(target, injector)

        /*
         * the only way to implement ctor injections, really
         */
        when ((parameters[1] as List<*>).size) {
            0 -> {
                try {
                    return construct(ctor as KClass<*>)
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }

                try {
                    val typeList = parameters[0] as List<Class<*>>
                    //val typeClasses = arrayOfNulls<KClass<*>>(typeList.size)
                    return construct(ctor as KClass<*>, typeList.toTypedArray())
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                }

            }
            else -> try {
                val typeList = parameters[0] as List<Class<*>>
                //val typeClasses = arrayOfNulls<KClass<*>>(typeList.size)
                //typeList.toTypedArray()
                return construct(ctor as KClass<*>, typeList.toTypedArray())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        return null
    }
}