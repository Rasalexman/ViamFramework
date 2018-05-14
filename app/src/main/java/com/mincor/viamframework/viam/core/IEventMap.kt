package com.mincor.viamframework.viam.core

import com.mincor.viamframework.viam.base.IEventListener
import kotlin.reflect.KClass

interface IEventMap {

    /**
     * map listener to event class
     */
    fun mapListener(dispatcher:IEventDispatcher, type:String, listener:IEventListener, eventClass: KClass<*>? = null)

    /**
     * Unmap listener from event class
     */
    fun unmapListener(dispatcher:IEventDispatcher, type:String, listener:IEventListener, eventClass:KClass<*>?)

    /**
     * Clear all listeners from mapListener
     */
    fun unmapAllListeners()

}