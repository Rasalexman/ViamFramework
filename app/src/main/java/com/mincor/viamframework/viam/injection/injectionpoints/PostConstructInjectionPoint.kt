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

package com.mincor.viamframework.viam.injection.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.injection.Injector

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Description：PostConstructInjectionPoint
 * Created by：CaMnter
 */
class PostConstructInjectionPoint
/*******************************************************************************************
 * public methods *
 */
/**
 * @param node     node
 * @param injector injector
 */
(node: XML, injector: Injector) : InjectionPoint(node, injector) {

    /*******************************************************************************************
     * private properties *
     */
    protected lateinit var methodName: String

    /**
     * Get the PostConstructInjectionPoint.this.orderValue
     *
     * @return int
     */
    var order: Int = 0
        protected set

    override fun applyInjection(target: Any, injector: Injector): Any {
        try {
            ((target as Map<String, Any>)[this.methodName] as Method)
                    .invoke(target)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return target
    }

    /*******************************************************************************************
     * protected methods *
     */
    override fun initializeInjection(node: XML) {
        val orderArg = node.getXMLListByName("arg").getXMLListByKeyValue(
                "key", "order")
        val methodNode = node.parent
        try {
            this.order = Integer.parseInt(orderArg[0]
                    .getValue("value"))
        } catch (e: Exception) {
            this.order = 0
        }

        this.methodName = methodNode!!.getValue("name")
    }

}