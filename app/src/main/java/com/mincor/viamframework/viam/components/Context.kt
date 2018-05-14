package com.mincor.viamframework.viam.components

import com.mincor.viamframework.viam.base.*
import com.mincor.viamframework.viam.injection.SuspendInjector
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.base.events.EventTypes
import com.mincor.viamframework.viam.core.*
import com.mincor.viamframework.viam.core.IInjector
import com.mincor.viamframework.viam.injection.SupendReflector


abstract class Context(override var contextView: Any? = null, private var autoStartup: Boolean = true) : ContextBase() {

    protected var injector: IInjector = SuspendInjector(null)
    protected var reflector: IReflector = SupendReflector()

    protected var interactorMap: IInteractorMap = InteractorMap(this.eventDispatcher, this.createChildInjector(), reflector)
    protected var viewControllerMap: IViewControllerMap = ViewControllerMap(reflector, contextView, this.createChildInjector())

    /**
     * private
     */
    protected var viewMap: IViewMap = ViewMap(contextView!!, injector)

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

    /**
     * private
     */
    protected fun checkAutoStartup() {
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
     * Override this in your Framework context to change the default
     * configuration
     * Beware of collisions in your container
     */
    protected fun mapInjections() {
        this.injector.mapValue(IReflector::class, reflector, "")
        this.injector.mapValue(IInjector::class, injector, "")
        this.injector.mapValue(IEventDispatcher::class, eventDispatcher, "")
        this.injector.mapValue(Any::class, contextView!!, "")
        this.injector.mapValue(IInteractorMap::class, interactorMap, "")
        this.injector.mapValue(IViewControllerMap::class, viewControllerMap,"")
        this.injector.mapValue(IViewMap::class, viewMap, "")
        this.injector.mapClass(IEventMap::class, EventMap::class, "")
    }

    /**
     * set your mvc relation
     * Add the view map
     * Link the View and View the corresponding Controller
     * Injection as an singleton, instantiate the singleton
     * Add Event (Event) with the connection of the Interactor
     */
    abstract fun setRelation()
}