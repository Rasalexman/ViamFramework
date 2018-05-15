package com.mincor.viamframework.viam.base.ext

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses

fun KClass<*>.className(): String {
    return this.qualifiedName ?: this.java.name
}

fun Annotation.className(): String {
    return this.annotationClass.qualifiedName ?: this.javaClass.name
}

fun <T:Any>MutableList<T>.addAndReturn(toAdd:T):MutableList<T>{
    this.add(toAdd)
    return this
}

fun <T> factoryCreator(factory: () -> T): T {
    val x: T = factory()
    return x
}

/**
 * Get the qualified class name
 *
 * @param value The class object
 * @return The name of the class
 */
fun Any.getQualifiedClassName(): String {
    return if (this is KClass<*>) {
        this.className()
    } else {
        this.javaClass.name
    }
}

/**
 * Get all the superclass object
 *
 * @param clazz The class object
 * @return A collection of all the superclass object
 */
fun KClass<*>.getSuperClasses(): MutableList<KClass<*>> = if (this.superclasses.isEmpty()) {
    arrayListOf()
} else {
    val superClass = this.superclasses.first()
    superClass.getSuperClasses().addAndReturn(superClass)
}

inline fun <reified T:Any> inject() = T::class.createInstance()

/**
 * Get a collection of all the implementation of the interface
 *
 * @param clazz The class object
 * @return A collection of all the implementation of the interface
 */
fun KClass<*>.getImplementsInterfaces(): List<KClass<*>> {
    val result = arrayListOf<KClass<*>>()
    val superClasses = this.getSuperClasses()
    superClasses.filter { it.java.isInterface }.mapTo(result, {it})
    return result
}

/**
 * Construct any object from class and vararg
 */
fun <T : Any> construct(kClass: KClass<T>, vararg args: Any?): T? {
    val ctor = kClass.primaryConstructor
    return ctor?.let {
        return@let if (it.parameters.isEmpty()) {
            ctor.call()
        } else if (!args.isEmpty()) {
            ctor.call(args)
        } else {
            var allOptional = true
            val argsVals = hashMapOf<KParameter, Any?>()

            it.parameters.forEach {
                if (!it.isOptional) {
                    allOptional = false
                    return@forEach
                }
                argsVals[it] = null
            }
            if(allOptional){
                ctor.callBy(argsVals)
            } else {
                throw Exception("ALL CONTRUCTOR PARAMETERS MUST BE OPTIONAL")
            }
        }
    }
}