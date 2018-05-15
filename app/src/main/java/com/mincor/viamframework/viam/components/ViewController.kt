package com.mincor.viamframework.viam.components

import com.mincor.viamframework.viam.base.EventMap
import com.mincor.viamframework.viam.base.IEventListener
import com.mincor.viamframework.viam.base.ViewControllerBase
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.IViewControllerMap
import com.mincor.viamframework.viam.core.Inject

abstract class ViewController : ViewControllerBase() {

    @field:Inject
    lateinit var contextView: Any

    @field:Inject
    lateinit var controllerMap: IViewControllerMap

    @field:Inject
    lateinit var eventDispatcher:IEventDispatcher

    val eventMap: EventMap by lazy { EventMap(eventDispatcher) }

    override fun preDetach() {
        this.eventMap.unmapAllListeners()
        super.preDetach()
    }

    /**
     * Dispatch helper method
     *
     * @param event The `Event` to dispatch on the
     * `IContext`'s `IEventDispatcher`
     * @return Boolean
     */
    protected fun dispatch(event: Event): Boolean = if (eventDispatcher.hasEventListener(event.type)) this.eventDispatcher.dispatchEvent(event) else false
}

fun ViewController.mapEvent(eventName:String, eventHandler:IEventListener){
    eventMap.mapListener(eventDispatcher, eventName, eventHandler)
}