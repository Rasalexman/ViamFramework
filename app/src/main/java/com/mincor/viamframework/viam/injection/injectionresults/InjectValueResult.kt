package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector
import com.mincor.viamframework.viam.injection.injectionresults.InjectionResult

class InjectValueResult(private val value: Any) : InjectionResult() {
    override fun getResponse(injector: Injector): Any = this.value
}