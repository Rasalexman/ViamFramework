package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viam.injection.Injector

class InjectValueResult(private val value: Any) : IInjectionResult {
    override fun getResponse(injector: Injector): Any = this.value
}