package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class InjectClassResult(private val responseType: KClass<*>) : IInjectionResult {
    override fun getResponse(injector: Injector): Any? = injector.instantiate(this.responseType)
}