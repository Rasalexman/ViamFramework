package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector
import kotlin.reflect.KClass

class InjectClassResult(private val m_responseType: KKClass<*>) : InjectionResult() {
    override fun getResponse(injector: Injector): Any? {
        return injector.instantiate(this.m_responseType)
    }
}