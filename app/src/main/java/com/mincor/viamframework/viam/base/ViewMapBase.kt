package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.core.IContextViewHolder
import com.mincor.viamframework.viam.core.IInjector


abstract class ViewMapBase(protected var injector: IInjector) : IContextViewHolder {

    /**
     * private
     */
    protected var viewListenerCount: Int = 0

    override var contextView: Any? = null
        set(value) {
            if (value !== field) {
                this.removeListeners()
                field = value
                if (this.viewListenerCount > 0) {
                    this.addListeners()
                }
            }
        }


    // ---------------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------------
    protected abstract fun addListeners()
    protected abstract fun removeListeners()
    protected abstract fun onViewAdded(e: Any)

}
