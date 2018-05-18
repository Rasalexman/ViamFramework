package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector
import com.mincor.viamframework.viam.injection.injectionresults.InjectionResult

class InjectOtherRuleResult(private val rule: InjectionConfig) : InjectionResult() {
    override fun getResponse(injector: Injector): Any? = this.rule.getResponse(injector)
}