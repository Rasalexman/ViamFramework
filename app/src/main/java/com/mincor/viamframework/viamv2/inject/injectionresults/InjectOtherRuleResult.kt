package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector

class InjectOtherRuleResult(private val rule: InjectionConfig) : IInjectionResult {
    override fun getResponse(injector: Injector): Any? = this.rule.getResponse(injector)
}