package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.Injector

open class InjectionPoint(node: XML, injector: Injector?) {
    init {
        this.initializeInjection(node)
    }

    /**
     * Apply the injection
     *
     * @param target   target
     * @param injector injector
     * @return Object
     */
    open fun applyInjection(target: Any, injector: Injector): Any? {
        return target
    }

    /*******************************************************************************************
     * protected methods *
     */
    protected open fun initializeInjection(node: XML) {}

}