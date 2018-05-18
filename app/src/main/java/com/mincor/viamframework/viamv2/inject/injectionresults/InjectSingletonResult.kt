package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class InjectSingletonResult(private val responseType: KClass<*>) : IInjectionResult {
    private var response: Any? = null

    override fun getResponse(injector: Injector): Any {
        this.response = this.response ?: injector.instantiate(this.responseType)
        return this.response!!
    }
}