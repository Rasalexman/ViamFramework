package com.mincor.viamframework.viam.core

import com.mincor.viamframework.viam.base.events.Event

//typealias Listener = ()->Unit

interface IEventDispatcher {


    fun addEventListener(type:String, listener:IListener)

    fun removeEventListener(type:String, listener:IListener)

    fun dispatchEvent(event: Event)

    fun hasEventListener(type:String):Boolean

}