package com.mincor.viamframework.viam.injection

import android.util.Log
import com.mincor.viamframework.viam.injection.injectionresults.InjectionResult

class InjectionConfig(var request: Class<*>, var injectionName: String) {

    var m_injector: Injector? = null
    var m_result: InjectionResult? = null

    fun getResponse(injector: Injector): Any? {
        if (this.m_result != null) {
            return this.m_result!!
                    .getResponse(this.m_injector?:injector)
        }
        val parentConfig = (this.m_injector?:injector)
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
        if (this.m_result != null)
            return true

        val parentConfig = (this.m_injector?:injector)
                .getAncestorMapping(this.request, this.injectionName)
        return parentConfig != null
    }

    /**
     * Determine whether there was a own response
     *
     * @return Boolean
     */
    fun hasOwnResponse(): Boolean? {
        return this.m_result != null
    }

    /**
     * Set the result(this.m_result)
     *
     * @param result result
     */
    fun setResult(result: InjectionResult?) {
        if (this.m_result != null && result != null) {
            Log.w("InjectionConfig",
                    "Warning: Injector already has a rule for type \""
                            + this.request.name
                            + "\", named \""
                            + this.injectionName
                            + "\".\n "
                            + "If you have overwritten this mapping intentionally you can use "
                            + "\"injector.unmap()\" prior to your replacement mapping in order to "
                            + "avoid seeing this message.")
        }
        this.m_result = result
    }
}
