package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.base.prototypes.XMLList
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses

object Base {

    /*fun getFieldValueByName(fieldName: String, obj: KClass<*>): Any? {
        return try {
            val field = obj.getField(fieldName)
            field?.get(obj)
        } catch (e: Exception) {
            null
        }
    }*/

    /**
     * Get the qualified class name
     *
     * @param value The class object
     * @return The name of the class
     */
    fun getQualifiedClassName(value: Any): String {
        return if (value is KClass<*>) {
            value.qualifiedName ?: value.toString()
        } else {
            value.javaClass.name
        }
    }

    /**
     * Get all the superclass object
     *
     * @param clazz The class object
     * @return A collection of all the superclass object
     */
    private fun getSuperClasses(clazz: KClass<*>): MutableList<KClass<*>> = if (clazz.superclasses.isEmpty()) {
        arrayListOf()
    } else {
        val superClass =  clazz.superclasses.first()
        val superClasses = getSuperClasses(superClass)
        superClasses.add(superClass)
        superClasses
    }

    /**
     * Get a collection of all the implementation of the interface
     *
     * @param clazz The class object
     * @return A collection of all the implementation of the interface
     */
    private fun getImplementsInterfaces(clazz: KClass<*>): List<KClass<*>> {
        val result = arrayListOf<KClass<*>>()
        val superClasses = getSuperClasses(clazz)
        superClasses.filter { it.java.isInterface }.mapTo(result, {it})
        return result
    }

    /**
     * *************************************************************************
     * About a class of XML description file (including the parent class,
     * interface, and public mathematics, common methods, the description of the
     * constructor)
     *
     * @param clazz A class object
     * @return A class of XML description file
     * *****************************************************************
     */
    fun describeType(clazz: KClass<*>): XML {
        val xml = XML()
        val name = clazz.qualifiedName ?: ""
        xml.name = "type"
        xml.prototype["name"] = name
        xml.child = XMLList()
        /********************************* Base Class  */
        val superClasses = getSuperClasses(clazz)
        /********************************* Interface  */
        val implementsInterfaces = getImplementsInterfaces(clazz)
        /********************************* FIELDS  */
        val fields = clazz.declaredMemberProperties
        /********************************* METHODS  */
        val methods = clazz.declaredMemberFunctions
        /********************************* Constructors  */
        val constructors = clazz.constructors

        /*
         * One by one to get superClasses
         * Then the child nodes of the associated to the main XML
         */
        superClasses.forEach {
            val extendsClassXml = XML()
            xml.child.add(extendsClassXml)
            extendsClassXml.name = "extendsClass"
            extendsClassXml.prototype["type"] = it.qualifiedName?:""
            extendsClassXml.parent = xml
        }

        /*
         * Create a factory XML
         *  And then link to the main XML's parent
         */
        val factoryXml = XML()
        xml.child.add(factoryXml)
        factoryXml.name = "factory"
        factoryXml.prototype["type"] = name
        factoryXml.child = XMLList()
        factoryXml.parent = xml

        /*
         * One by one to get superClasses
         * Then the child nodes of the associated to the main XML
         */
        superClasses.forEach {
            val extendsClassXml = XML()
            factoryXml.child.add(extendsClassXml)
            extendsClassXml.name = "extendsClass"
            extendsClassXml.prototype["type"] = it.qualifiedName?:""
            extendsClassXml.parent = factoryXml
        }

        /*
         * One by one to get implementsInterfaces
         * Then the child nodes of the associated to the main XML
         */
        implementsInterfaces.forEach {
            val interfaceXml = XML()
            factoryXml.child.add(interfaceXml)
            interfaceXml.name = "implementsInterface"
            interfaceXml.prototype["type"] = it.qualifiedName?:""
            interfaceXml.parent = factoryXml
        }

        /*
         * One by one to get constructors
         * And get the constructor parameters one by one
         * The first constructor parameter associated with the child
         * nodes of the constructors
         * Then the constructors nodes associated with factoryXml child
         * nodes
         */
        constructors.forEach { constructor ->
            val constructorXml = XML()
            factoryXml.child.add(constructorXml)
            constructorXml.name = "constructor"
            constructorXml.parent = factoryXml

            constructor.typeParameters.forEachIndexed { i, param ->
                val parameterXml = XML()
                constructorXml.child.add(parameterXml)
                parameterXml.name = "parameter"
                parameterXml.prototype["index"] = "$i"
                parameterXml.prototype["type"] = param.name
                parameterXml.parent = constructorXml
            }
        }

        /*
         * One by one to get fields
         * Then the child nodes of the associated to the main XML
         */
        fields.forEach { field ->
            val fieldXml = XML()
            factoryXml.child.add(fieldXml)
            fieldXml.name = "variable"
            fieldXml.prototype["name"] = field.name
            fieldXml.prototype["type"] = field.getter.name
            fieldXml.parent = factoryXml
            val ans = field.annotations.toList()

            /*
             * One by one to get annotations
             * Then the associated to the fields of child nodes
             */
            ans.forEach { an ->
                val metadataXml = XML()
                fieldXml.child.add(metadataXml)
                metadataXml.name = "metadata"
                metadataXml.prototype["name"] = an.annotationClass.qualifiedName?:""
                metadataXml.parent = fieldXml
            }
        }

        /*
         * One by one to get methods
         * Then associated with factoryXml child nodes
         */
        methods.forEach { method ->
            val methodXml = XML()
            factoryXml.child.add(methodXml)
            methodXml.name = "method"
            methodXml.prototype["name"] = method.name
            methodXml.prototype["declaredBy"] = method.name
            methodXml.prototype["returnType"] = method.returnType.toString()
            methodXml.parent = factoryXml
            val parameterTypes = method.typeParameters
            val ans = method.annotations

            /*
             * One by one to get annotations
             * Then associated with the methods of child nodes
             */
            ans.forEach { an ->
                val metadataXml = XML()
                methodXml.children().add(metadataXml)
                metadataXml.name = "metadata"
                metadataXml.prototype["name"] = an.annotationClass.qualifiedName?:""
                metadataXml.parent = methodXml
            }

            /*
             * One by one to get parameterTypes
             * Then associated with the methods of child nodes
             */
            parameterTypes.forEachIndexed { index, kTypeParameter ->
                val parameterXml = XML()
                methodXml.child.add(parameterXml)
                parameterXml.name = "parameter"
                parameterXml.prototype["index"] = "$index"
                parameterXml.prototype["type"] = kTypeParameter.name
                parameterXml.prototype["optional"] = "false"
                parameterXml.parent = methodXml
            }
        }
        return xml
    }

}
