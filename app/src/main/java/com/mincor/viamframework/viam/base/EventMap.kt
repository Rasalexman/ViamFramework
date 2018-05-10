package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.IEventMap
import com.mincor.viamframework.viam.core.IListener

typealias IEventListener = (event:Event)->Unit

class EventMap(var eventDispatcher: IEventDispatcher, private val listeners: ArrayList<Params> = arrayListOf()) : IEventMap {

    var isEnabled: Boolean = true

    override fun mapListener(dispatcher: IEventDispatcher, type: String, listener: IEventListener, eventClass: Class<*>?) {
        if (!this.isEnabled && dispatcher === this.eventDispatcher) {
            throw ContextError(ContextError.E_EVENTMAP_NOSNOOPING)
        }

        val tempClass = eventClass ?: Event::class.java

        if (this.listeners.any {
                    it.dispatcher === dispatcher && it.type.equals(type)
                            && it.listener === listener
                            && it.eventClass === eventClass
                }) return


        val callback = Callback(listener, tempClass)
        val params = Params()
        params.dispatcher = dispatcher
        params.type = type
        params.listener = listener
        params.eventClass = eventClass
        params.callback = callback
        this.listeners.add(params)
        dispatcher.addEventListener(type, callback)
    }

    override fun unmapListener(dispatcher: IEventDispatcher, type: String, listener: IEventListener, eventClass: Class<*>?) {
        val tempClass = eventClass ?: Event::class.java
        this.listeners.filter {
            it.dispatcher === dispatcher && it.type == type
                    && it.listener === listener
                    && it.eventClass == tempClass
        }.forEach {
            dispatcher.removeEventListener(type, it.callback)
            this.listeners.remove(it)
        }
    }

    override fun unmapAllListeners() {
        var params: Params?
        var dispatcher: IEventDispatcher?
        while (this.listeners.size > 0) {
            params = this.listeners.removeAt(this.listeners.size - 1)
            dispatcher = params.dispatcher
            dispatcher?.removeEventListener(params.type, params.callback)
        }
    }

    /**
     * Event Handler

     * @param event              The `Event`
     * @param listener           listener
     * @param originalEventClass originalEventClass
     */
    private fun routeEventToListener(event: Event, listener: IEventListener, originalEventClass: Class<*>) {
        if (originalEventClass.isInstance(event)) {
            listener(event)
        }
    }

    /**
     * Used to save the EventMap.this.listener set out in the instance of the
     * object
     */
    data class Params(
            var dispatcher: IEventDispatcher? = null,
            var type: String? = null,
            var listener: IEventListener? = null,
            var eventClass: Class<*>? = null,
            var callback: IListener? = null
    )

    /**
     * Used to instantiate the IEventListener callback in
     * EventMap.Params.this.listener
     */
    private inner class Callback(private val listener: IEventListener, private val eventClass: Class<*>) : Listener() {
        override fun onEventHandle(event: Event) {
            routeEventToListener(event, listener, eventClass)
        }
    }

}