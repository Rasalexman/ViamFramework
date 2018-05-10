package com.mincor.viamframework.viam.core

import com.mincor.viamframework.viam.base.IEventListener

interface IEventMap {

    /**
     * map listener to event class
     */
    fun mapListener(dispatcher:IEventDispatcher, type:String, listener:IEventListener, eventClass:Class<*>? = null)

    /**
     * Unmap listener from event class
     */
    fun unmapListener(dispatcher:IEventDispatcher, type:String, listener:IEventListener, eventClass:Class<*>?)

    /**
     * Clear all listeners from mapListener
     */
    fun unmapAllListeners()

}