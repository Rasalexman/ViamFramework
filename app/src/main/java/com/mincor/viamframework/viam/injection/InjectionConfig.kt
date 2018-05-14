package com.mincor.viamframework.viam.injection

import android.util.Log
import com.mincor.viamframework.viam.injection.injectionresults.InjectionResult
import kotlin.reflect.KClass

class InjectionConfig(var request: KClass<*>, var injectionName: String) {

    private var injector: Injector? = null
    private var result: InjectionResult? = null

    fun getResponse(injector: Injector): Any? {
        if (this.result != null) {
            return this.result!!
                    .getResponse(this.injector?:injector)
        }
        val parentConfig = (this.injector?:injector)
                .getAncestorMapping(this.request, this.injectionName)
        return parentConfig?.getResponse(injector)
    }

    /**
     * Determine whether there was a response
     *
     * @param injector injector
     * @return Boolean
     */
    fun hasResponse(injector: Injector): Boolean {
        if (this.result != null)
            return true

        val parentConfig = (this.injector?:injector)
                .getAncestorMapping(this.request, this.injectionName)
        return parentConfig != null
    }

    /**
     * Determine whether there was a own response
     *
     * @return Boolean
     */
    fun hasOwnResponse(): Boolean? {
        return this.result != null
    }

    /**
     * Set the result(this.result)
     *
     * @param result result
     */
    fun setResult(result: InjectionResult?) {
        if (this.result != null && result != null) {
            Log.w("InjectionConfig",
                    "Warning: Injector already has a rule for type \""
                            + this.request.java.name
                            + "\", named \""
                            + this.injectionName
                            + "\".\n "
                            + "If you have overwritten this mapping intentionally you can use "
                            + "\"injector.unmap()\" prior to your replacement mapping in order to "
                            + "avoid seeing this message.")
        }
        this.result = result
    }
}
