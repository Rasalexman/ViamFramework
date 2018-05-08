package com.mincor.viamframework.viam.core

//typealias Listener = ()->Unit

interface IEventDispatcher {


    fun addEventListener(type:String, listener:IListener)

    fun removeEventListener(type:String, listener:IListener?)

    fun dispatchEvent(event:Any):Boolean

    fun hasEventListener(type:String)

}