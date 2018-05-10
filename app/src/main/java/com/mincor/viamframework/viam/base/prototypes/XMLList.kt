package com.mincor.viamframework.viam.base.prototypes

class XMLList : ArrayList<XML>() {

    /**
     * Obtain XMLList node under each child node in an XML list This is a
     * recursive process, XMLList -  XML -  XMLList -  XML...
     *
     * @return new an XMLList of adding all the nodes
     */
    fun children(): XMLList {
        val result = XMLList()
        repeat(this.size) {
            result.addAll(this[it].children())
        }
        return result
    }

    /**
     * Search for XML name attribute of the same XML all child nodes
     *
     * @param name The XML's name attribute XML
     * @return Meet the conditions of the child nodes of the list
     */
    fun findXMLListByName(name: String): XMLList {
        val result = XMLList()
        this.filter { it.name == name }.mapTo(result, { it })
        return result
    }

    /**
     * Search for the prototype in a XML key and the value of the same XML all
     * child nodes
     *
     * @param key   The key to the prototype of XML XML
     * @param value The value to the prototype of XML XML
     * @return Meet the conditions of the child nodes of the list
     */
    fun findXMLListByKeyValue(key: String, value: String): XMLList {
        val result = XMLList()
        this.filter { it.getValue(key) == value }.mapTo(result, { it })
        return result
    }

    /**
     * *************************************************************************
     * Get all the same name attribute node (all nodes and child nodes)
     *
     * @param name The XML's name attribute XML
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByName(name: String): XMLList {
        val result = XMLList()
        this.forEach { result.addAll(it.getXMLListByName(name)) }
        return result
    }

    /**
     * *************************************************************************
     * Get all the prototype property at the same key nodes (all nodes and child
     * nodes)
     *
     * @param key The key of the prototype properties prototype
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByKey(key: String): XMLList {
        val result = XMLList()
        this.forEach { result.addAll(it.getXMLListByKey(key)) }
        return result
    }

    /**
     * *************************************************************************
     * Obtain all share the same name and prototype property key nodes (all
     * nodes and child nodes)
     *
     * @param name The XML's name attribute XML
     * @param key  The key of the prototype properties
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByNameAndKey(name: String, key: String): XMLList {
        val result = XMLList()
        this.forEach { result.addAll(it.getXMLListByNameAndKey(name, key)) }
        return result
    }

    /**
     * *************************************************************************
     * Get all the attributes of the prototype and prototype same properties key
     * value of the same node (all nodes and child nodes)
     *
     * @param key   The key of the prototype properties
     * @param value The value of the prototype properties
     * * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByKeyValue(key: String, value: String): XMLList {
        val result = XMLList()
        this.forEach { result.addAll(it.getXMLListByKeyValue(key, value)) }
        return result
    }

    /**
     * *************************************************************************
     * Get all the attributes of the same name and prototype key value
     * corresponding to the node (all nodes and child nodes)
     *
     * @param name  The XML's name attribute XML
     * @param key   The key of the prototype properties
     * @param value The value of the prototype properties
     * @return Meet the conditions of the child nodes of the list
     * *****************************************************************
     */
    fun getXMLListByNameAndKeyValue(name: String, key: String, value: String): XMLList {
        val result = XMLList()
        this.forEach { result.addAll(it.getXMLListByNameAndKeyValue(name, key, value)) }
        return result
    }
}