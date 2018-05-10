package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.base.events.EventDispatcher
import com.mincor.viamframework.viam.core.IContextViewHolder
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.IListener

abstract class ContextBase : IContextViewHolder, IEventDispatcher {

    override var contextView: Any? = null
    override var isEnabled: Boolean = true

    protected val eventDispatcher:IEventDispatcher = EventDispatcher()

    override fun addEventListener(type: String, listener: IListener) {
        this.eventDispatcher.addEventListener(type, listener)
    }

    override fun removeEventListener(type: String?, listener: IListener?) {
        eventDispatcher.removeEventListener(type, listener)
    }

    override fun dispatchEvent(event: Event):Boolean = eventDispatcher.dispatchEvent(event)
    override fun hasEventListener(type: String): Boolean = eventDispatcher.hasEventListener(type)


}