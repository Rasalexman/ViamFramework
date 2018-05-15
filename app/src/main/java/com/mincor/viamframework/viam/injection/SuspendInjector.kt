package com.mincor.viamframework.viam.injection

import com.mincor.viamframework.viam.core.IInjector


class SuspendInjector : Injector(), IInjector {

    override fun createChild(): IInjector {
        val injector = SuspendInjector()
        injector.parentInjector = this
        injector.createChildInjector()
        return injector
    }
}