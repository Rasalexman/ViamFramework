package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector

class InjectClassResult(private val m_responseType: Class<*>) : InjectionResult() {
    override fun getResponse(injector: Injector): Any? {
        return injector.instantiate(this.m_responseType)
    }
}