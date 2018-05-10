package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.InjectionConfig
import com.mincor.viamframework.viam.injection.Injector

class InjectOtherRuleResult(private val m_rule: InjectionConfig) : InjectionResult() {
    override fun getResponse(injector: Injector): Any? {
        return this.m_rule.getResponse(injector)
    }

}