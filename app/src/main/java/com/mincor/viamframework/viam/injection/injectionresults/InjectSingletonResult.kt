/*
 * Copyright (C) 2015 CaMnter yuanyu.camnter@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mincor.viamframework.viam.injection.injectionresults

import com.mincor.viamframework.viam.injection.Injector

/**
 * Description：InjectSingletonResult
 * Created by：CaMnter
 */
class InjectSingletonResult(private val m_responseType: Class<*>) : InjectionResult() {
    private var m_response: Any? = null

    override fun getResponse(injector: Injector): Any {
        this.m_response = this.m_response ?: this.createResponse(injector)
        return this.m_response!!
    }

    private fun createResponse(injector: Injector): Any? {
        return injector.instantiate(this.m_responseType)
    }

}