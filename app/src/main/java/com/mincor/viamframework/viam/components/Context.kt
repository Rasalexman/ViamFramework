package com.mincor.viamframework.viam.components

import com.mincor.viamframework.viam.base.*
import com.mincor.viamframework.viam.injection.SuspendInjector
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.base.events.EventTypes
import com.mincor.viamframework.viam.core.*
import com.mincor.viamframework.viam.core.IInjector


abstract class Context(override var contextView: Any? = null, var autoStartup: Boolean = true) : ContextBase() {

    protected val injector: IInjector by lazy {  SuspendInjector() }

    protected val interactorMap: IInteractorMap by lazy { InteractorMap(this.eventDispatcher, this.createChildInjector()) }
    protected val viewControllerMap: IViewControllerMap by lazy { ViewControllerMap(contextView, this.createChildInjector()) }

    /**
     * private
     */
    private val viewMap: IViewMap by lazy { ViewMap(contextView!!, injector) }

    init {
        mapInjections()
        checkAutoStartup()
    }

    fun startup() {
        this.dispatchEvent(Event(EventTypes.STARTUP_COMPLETE))
        this.setRelation()
    }

    fun shutdown() {
        this.dispatchEvent(Event(EventTypes.SHUTDOWN_COMPLETE))
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
     * private
     * @return IInjector
     */
    protected fun createChildInjector(): IInjector = this.injector.createChild()

    /**
     * @param view view
     */
    fun injectViewController(view: Any) {
        this.viewControllerMap.addController(view)
    }

    /**
     * @param view view
     */
    fun removeViewController(view: Any) {
        this.viewControllerMap.unInjectController(view)
    }

    /**
     * Injection Mapping Hook
     */
    private fun mapInjections() {
        this.injector.mapValue(IInjector::class, injector)
        this.injector.mapValue(IEventDispatcher::class, eventDispatcher)
        this.injector.mapValue(Any::class, contextView!!)
        this.injector.mapValue(IInteractorMap::class, interactorMap)
        this.injector.mapValue(IViewControllerMap::class, viewControllerMap)
        this.injector.mapValue(IViewMap::class, viewMap)
        this.injector.mapClass(IEventMap::class, EventMap::class)
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