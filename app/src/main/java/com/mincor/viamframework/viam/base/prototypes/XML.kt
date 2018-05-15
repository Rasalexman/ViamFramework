package com.mincor.viamframework.viam.base.prototypes

class XML {

    var name: String = ""
    var prototype = hashMapOf<String, String>()
    var child = XMLList()
    var parent: XML? = null


    /**
     * Add an XML node, the XML node's parent node points to the corresponding
     * XMLList
     *
     * @param childXml To add an XML node child
     * @return XML node is added XML
     */
    fun appendChild(childXml: XML): XML {
        this.children().add(childXml)
        childXml.parent = this
        return this
    }

    /**
     * According to the name for the first child (XML)
     * If so, it returns the corresponding XML node, not return a new XML
     *
     * @param name XML's name XML
     * @return Corresponding to the name of the XML
     */
    fun getXMLByName(name: String): XML {
        if (ArrayList::class.isInstance(this.child)) {
            val childList = this.child
            val findedXML = childList.find { it.name == name}
            if(findedXML != null){
                return findedXML
            }
        }
        return XML()
    }

    /**
     * Whether child for XMLList instance, is converted to XMLList, will Object
     * type of child and return
     *
     * @return <code>XMLList</code> this.child object after conversion
     * this.child XMLList
     */
    fun children(): XMLList {
        var result = XMLList()
        if (XMLList::class.isInstance(this.child)) {
            result = this.child
        }
        return result
    }

    /**
     * *************************************************************************
     * Get an XMLList contains all child nodes
     * Here is the child node traverse all the nodes, it is a recursive process
     *
     * @return new an XMLList of adding all the nodes new XMLList
     * *****************************************************************
     */
    fun getAllChildren(): XMLList {
        val result = XMLList()
        val children = this.children()
        result.addAll(children)
        children.forEach { result.addAll(it.getAllChildren()) }
        return result
    }

    /**
     * Set the value of the this.prototype
     *
     * @param key   To add the key
     * @param value To add the value
     * @return The `XML`
     */
    fun setValue(key: String, value: String): XML {
        this.prototype[key] = value
        return this
    }

    /**
     *
     * @param key this.prototype's key this.prototype
     * @return The key value corresponding to the value key
     */
    fun getValue(key: String): String = this.prototype[key] ?: ""

    /**
     * According to the name list of child nodes
     *
     * @param name XML's name XML
     * @return The same name a list of child nodes
     */
    fun getXMLListByName(name: String): XMLList {
        val result = XMLList()
        if (ArrayList::class.isInstance(this.child)) {
            val childList = this.child
            childList.filter { it.name == name }.mapTo(result, { it })
        }
        return result
    }

    /**
     * *************************************************************************
     * Get the child node list according to the prototype of the key
     *
     * @param key this.prototype's key this.prototype
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByKey(key: String): XMLList {
        val result = XMLList()
        if (ArrayList::class.isInstance(this.child)) {
            val childList = this.child
            childList.filter { it.prototype.containsKey(key) }.mapTo(result, { it })
        }
        return result
    }

    /**
     * *************************************************************************
     * According to the name attribute of XML and XML key access list of child
     * nodes of the prototype
     *
     * @param name XML's name XML
     * @param key  this.prototype's key this.prototype
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByNameAndKey(name: String, key: String): XMLList {
        val result = XMLList()
        if (ArrayList::class.isInstance(this.child)) {
            val childList = this.child
            childList.filter { it.prototype.containsKey(key) && it.name == name }.mapTo(result, { it })
        }
        return result
    }

    /**
     * *************************************************************************
     * According to the prototype of XML key and the value for the list of child
     * nodes
     *
     * @param key   this.prototype's key this.prototype
     * @param value this.prototype's value this.prototype
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByKeyValue(key: String, value: String): XMLList {
        val result = XMLList()
        if (ArrayList::class.isInstance(this.child)) {
            val childList = this.child
            childList.filter { it.prototype.containsKey(key) && it.prototype[key] == value }.mapTo(result, { it })
        }
        return result
    }

    /**
     * *************************************************************************
     * According to the name attribute XML, XML, the prototype of the key and
     * the value of access list of child nodes
     *
     * @param name  XML's name XML
     * @param key   this.prototype's key this.prototype
     * @param value this.prototype's value this.prototype
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByNameAndKeyValue(name: String, key: String, value: String): XMLList {
        val result = XMLList()
        if (ArrayList::class.isInstance(this.child)) {
            val childList = this.child
            childList.filter {
                it.prototype.containsKey(key)
                    && it.prototype[key] == value
                    && it.name == name}.mapTo(result, { it })
        }
        return result
    }
}