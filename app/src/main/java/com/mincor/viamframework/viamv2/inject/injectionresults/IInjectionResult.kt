package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viam.injection.Injector

interface IInjectionResult {
    fun getResponse(injector: Injector): Any?
}
