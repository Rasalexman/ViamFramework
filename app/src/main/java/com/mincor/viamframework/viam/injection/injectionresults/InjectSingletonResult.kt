package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class InjectSingletonResult(private val responseType: KClass<*>) : InjectionResult() {
    private var m_response: Any? = null

    override fun getResponse(injector: Injector): Any {
        this.m_response = this.m_response ?: injector.instantiate(this.responseType)
        return this.m_response!!
    }
}