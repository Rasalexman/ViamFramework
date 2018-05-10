package com.mincor.viamframework.viam.base.events

import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.IListener

class EventDispatcher : IEventDispatcher {

    companion object {
        private val eventDispatchers = hashMapOf<String, IEventDispatcher>()
        private val listeners = arrayListOf<IListener>()

        fun removeDispatcher(name:String):IEventDispatcher? = eventDispatchers.remove(name)
        fun setDispatcher(name:String):IEventDispatcher = eventDispatchers.getOrPut(name, {EventDispatcher()})
        fun getDispatcher(name:String):IEventDispatcher? = eventDispatchers[name]
    }

    override fun addEventListener(type: String, listener: IListener) {
        listener.type = type
        listeners.add(listener)
    }

    override fun removeEventListener(type: String?, listener: IListener?) {
        listeners.filter { it.type == type && it.name == listener?.name }.forEach { listeners.remove(it) }
    }

    override fun dispatchEvent(event: Event):Boolean {
        listeners.filter { it.type == event.type }.forEach { it.onEventHandle(event) }
        return true
    }

    override fun hasEventListener(type: String):Boolean = listeners.any { it.type == type }
}