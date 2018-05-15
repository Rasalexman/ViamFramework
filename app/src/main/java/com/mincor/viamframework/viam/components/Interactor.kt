package com.mincor.viamframework.viam.components

import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.*


abstract class Interactor {

    @field:Inject
    var contextView: Any? = null

    @field:Inject
    var commandMap: IInteractorMap? = null

    @field:Inject
    lateinit var eventDispatcher: IEventDispatcher

    @field:Inject
    var injector: IInjector? = null

    @field:Inject
    var mediatorMap: IViewControllerMap? = null

    /**
     * subclass must inherit the execute method
     */
    abstract fun execute()

    /**
     * Dispatch helper method
     *
     * @param event The `Event` to dispatch on the
     * `IContext`'s `IEventDispatcher`
     * @return Boolean
     */
    protected fun dispatch(event: Event): Boolean = if (eventDispatcher.hasEventListener(event.type)) this.eventDispatcher.dispatchEvent(event) else false


}