package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viamv2.inject.Injector
import com.mincor.viamframework.viamv2.inject.instantiate
import kotlin.reflect.KClass

class InjectClassResult(private val clazz:KClass<*>) : IInjectionResult {
    override fun getResponse(injector: Injector): Any? = injector.instantiate(clazz)
}