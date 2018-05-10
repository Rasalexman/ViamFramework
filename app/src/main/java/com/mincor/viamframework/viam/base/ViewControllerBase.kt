package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viam.core.IViewController


abstract class ViewControllerBase : IViewController {

    /**
     * Internal
     * You should declare a dependency on a concrete view component
     * in your implementation instead of working with this property
     */
    override var view: Any? = null

    /**
     * Internal
     * In the case of differed instantiation, onRemove might get called before
     * onCreationComplete has fired. This here Boolean helps us track that
     * scenario.
     */
    protected var removed: Boolean = false

    override fun preAttach() {
        this.removed = false
        this.onAttach()
    }

    override fun preDetach() {
        this.removed = true
        this.onDetach()
    }

    protected fun onCreationComplete(event: Event) {
        (event.target as IEventDispatcher).removeEventListener(
                "creationComplete", CreationCompleteListener(
                "creationComplete", "onCreationComplete"))
        if (!this.removed) {
            this.onAttach()
        }
    }

    private inner class CreationCompleteListener(type: String, name: String) : Listener(type, name) {
        override fun onEventHandle(event: Event) {
            onCreationComplete(event)
        }
    }
}