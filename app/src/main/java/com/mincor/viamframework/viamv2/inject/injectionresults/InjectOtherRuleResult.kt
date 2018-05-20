package com.mincor.viamframework.viamv2.inject.injectionresults

import com.mincor.viamframework.viamv2.inject.Injector
import com.mincor.viamframework.viamv2.inject.data.Config

class InjectOtherRuleResult(private val rule: Config) : IInjectionResult {
    override fun getResponse(injector: Injector): Any? = this.rule.result?.getResponse(injector)
}