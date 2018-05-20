package com.mincor.viamframework.viam.base.ext

import com.mincor.viamframework.viam.base.Base
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

fun classExtendsOrImplements(classOrClassName: Any, superclass: KClass<*>): Boolean {
    var actualClass: KClass<*>? = null
    if (classOrClassName is KClass<*>) {
        actualClass = classOrClassName
    } else if (classOrClassName is String) {
        try {
            actualClass = Class.forName(classOrClassName).kotlin
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            throw Error("The class name " + classOrClassName
                    + " is not valid because of " + e + "\n"
                    + e.stackTrace)
        }

    }
    if (actualClass == null) {
        throw Error("The parameter classOrClassName must be a valid Class " + "instance or fully qualified class name.")
    }
    if (actualClass == superclass)
        return true

    val factoryDescription = Base.describeType(actualClass).getXMLByName("factory")
    val children = factoryDescription.children()
    children.forEach {
        if ((it.name == "implementsInterface" || it.name == "extendsClass") && it.getValue("type") == superclass.getQualifiedClassName()) {
            return true
        }
    }
    return false
}

fun getClass(value: Any): KClass<*> = value as? KClass<*> ?: value.javaClass.kotlin

fun getFullyQualifiedClassName(value: Any, replaceColons: Boolean): String {
    val fullyQualifiedClassName: String
    if (String::class.isInstance(value)) {
        fullyQualifiedClassName = value as String

        /*
         * Add colons if missing and desired.
         */
        if ((!replaceColons) && !fullyQualifiedClassName.contains("::")) {
            val lastDotIndex = fullyQualifiedClassName.lastIndexOf(".")
            return if (lastDotIndex == -1) fullyQualifiedClassName else fullyQualifiedClassName.substring(0, lastDotIndex)+ "::"+ fullyQualifiedClassName.substring(lastDotIndex + 1)
        }
    } else {
        fullyQualifiedClassName = value.getQualifiedClassName()
    }
    return if (replaceColons)
        fullyQualifiedClassName.replace("::", ".")
    else
        fullyQualifiedClassName
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