package com.mincor.viamframework.viamv2.base

import com.mincor.viamframework.viam.base.EventMap
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.base.events.EventDispatcher
import com.mincor.viamframework.viam.core.IContextViewHolder
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.IEventMap
import com.mincor.viamframework.viam.core.IListener
import com.mincor.viamframework.viamv2.inject.Injector
import com.mincor.viamframework.viamv2.inject.mapClass
import com.mincor.viamframework.viamv2.inject.mapValue


abstract class ViamContext(override var contextView: Any? = null, var autoStartup: Boolean = true) : Injector(), IContextViewHolder, IEventDispatcher {

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

    init {
        mapInjections()
        checkAutoStartup()
    }

    fun startup() {
        this.setRelation()
    }

    fun shutdown() {

    }

    // ---------------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------------
    private fun checkAutoStartup() {
        if (this.autoStartup && this.contextView != null) {
            this.startup()
        }
    }

    /**
     * @param view view
     */
    fun injectViewController(view: Any) {
        //this.viewControllerMap.addController(view)
    }

    /**
     * @param view view
     */
    fun removeViewController(view: Any) {
        //this.viewControllerMap.unInjectController(view)
    }

    /**
     * Injection Mapping Hook
     */
    private fun mapInjections() {
        val config = mapValue<IEventDispatcher>(eventDispatcher)
        val configClass = mapClass<IEventMap, EventMap>( "")
        println("CONFIG $config")
        //mapClass()
    }

    /**
     * set your relations
     * Add the view map
     * Link the View and View the corresponding Controller
     * Injection as an singleton, instantiate the singleton
     * Add Event (Event) with the connection of the Interactor
     */
    abstract fun setRelation()
}