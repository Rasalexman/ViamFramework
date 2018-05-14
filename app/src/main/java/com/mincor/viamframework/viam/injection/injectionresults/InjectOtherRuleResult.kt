package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector

class InjectOtherRuleResult(private val rule: InjectionConfig) : InjectionResult() {
    override fun getResponse(injector: Injector): Any? = this.rule.getResponse(injector)
}