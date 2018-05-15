package com.mincor.viamframework.viam.injection

import com.mincor.viamframework.viam.base.Base
import com.mincor.viamframework.viam.base.ext.getQualifiedClassName
import com.mincor.viamframework.viam.core.IReflector
import kotlin.reflect.KClass


abstract class Reflector : IReflector {

    /**
     * Judge whether a class inherits a class or implements an interface
     *
     * @param classOrClassName classOrClassName
     * @param superclass superclass
     * @return Boolean
     */
    override fun classExtendsOrImplements(classOrClassName: Any, superclass: KClass<*>): Boolean {
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

    override fun getClass(value: Any): KClass<*> {
        return value as? KClass<*> ?: value.javaClass.kotlin
    }

    /**
     * Get fully qualified class name
     *
     * @param value value
     * @param replaceColons replaceColons
     * @return String
     */
    override fun getFullyQualifiedClassName(value: Any, replaceColons: Boolean): String {
        val fullyQualifiedClassName: String
        if (String::class.java.isInstance(value)) {
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
}