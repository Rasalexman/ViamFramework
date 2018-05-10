package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector


abstract class InjectionResult {
    abstract fun getResponse(injector: Injector): Any?
}
