package com.mincor.viamframework.viam.core

interface IEventMap {

    /**
     * map listener to event class
     */
    fun mapListener(dispatcher:IEventDispatcher, type:String, listener:IListener, event:Any)

    /**
     * Unmap listener from event class
     */
    fun unmapListener(dispatcher:IEventDispatcher, type:String, listener:IListener, event:Any)

    /**
     * Clear all listeners from mapListener
     */
    fun unmapAllListeners()

}