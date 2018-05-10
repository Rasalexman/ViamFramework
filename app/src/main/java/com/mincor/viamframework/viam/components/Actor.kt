package com.mincor.viamframework.viam.components

import com.mincor.viamframework.viam.base.EventMap
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.Inject
import kotlin.reflect.KProperty

open class Actor {

    @field:Inject
    lateinit var eventDispatcher:IEventDispatcher

    val myLazyInitializer by MyDelegate()

    val eventMap: EventMap by lazy { EventMap(eventDispatcher) }



    /**
     * Dispatch helper method
     *
     * @param event The `Event` to dispatch on the
     * `IContext`'s `IEventDispatcher`
     * @return Boolean
     */
    protected fun dispatch(event: Event): Boolean = if (eventDispatcher.hasEventListener(event.type)) this.eventDispatcher.dispatchEvent(event) else false
}

class MyDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, спасибо за делегирование мне '${property.name}'!"
    }
}