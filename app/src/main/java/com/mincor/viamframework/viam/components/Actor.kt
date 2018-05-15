package com.mincor.viamframework.viam.components

import com.mincor.viamframework.viam.base.EventMap
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.Inject
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class Actor {

    @Inject
    lateinit var eventDispatcher:IEventDispatcher

    val myLazyInitializer:IEventDispatcher by inject()

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

inline fun <reified T:Any> inject(): ReadOnlyProperty<Actor, T> {
    return object : ReadOnlyProperty<Actor, T> {
        override fun getValue(thisRef: Actor, property: KProperty<*>): T {
            return property as T
        }
    }
}