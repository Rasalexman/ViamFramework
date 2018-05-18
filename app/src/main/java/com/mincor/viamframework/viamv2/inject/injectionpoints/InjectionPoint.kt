package com.mincor.viamframework.viamv2.inject.injectionpoints

import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viamv2.inject.IInjector
import kotlin.reflect.KClass

abstract class InjectionPoint(node: XML, injector: IInjector?) {

    abstract val methodName:String

    /**
     * Apply the injection
     *
     * @param target   target
     * @param injector injector
     * @return Object
     */
    open fun <T : KClass<*>>applyInjection(target: T?, injector: IInjector): T? {
        return target
    }
}