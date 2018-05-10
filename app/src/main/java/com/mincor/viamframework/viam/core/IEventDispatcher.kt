package com.mincor.viamframework.viam.core

import com.mincor.viamframework.viam.base.events.Event

interface IEventDispatcher {

    fun addEventListener(type:String, listener:IListener)

    fun removeEventListener(type:String?, listener:IListener?)

    fun dispatchEvent(event: Event):Boolean

    fun hasEventListener(type:String):Boolean

}