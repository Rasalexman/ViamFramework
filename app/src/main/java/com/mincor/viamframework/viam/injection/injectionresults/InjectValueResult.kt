package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector

class InjectValueResult(private val m_value: Any) : InjectionResult() {
    override fun getResponse(injector: Injector): Any {
        return this.m_value
    }

}