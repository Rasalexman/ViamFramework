package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viamv2.inject.IInjector
import com.mincor.viamframework.viamv2.inject.Injector
import com.mincor.viamframework.viamv2.inject.instantiate
import kotlin.reflect.KClass

class InjectSingletonResult(private val responseType: KClass<*>) : IInjectionResult {
    private var response: Any? = null

    override fun getResponse(injector: Injector): Any? {
        this.response = this.response ?: injector.instantiate(responseType)
        return this.response!!
    }
}