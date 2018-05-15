package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class InjectClassResult(private val responseType: KClass<*>) : InjectionResult() {
    override fun getResponse(injector: Injector): Any? = injector.instantiate(this.responseType)
}