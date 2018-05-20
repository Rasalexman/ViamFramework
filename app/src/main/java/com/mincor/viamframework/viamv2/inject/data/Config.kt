package com.mincor.viamframework.viamv2.inject.data

import android.util.Log
import com.mincor.viamframework.viamv2.inject.getAncestorMapping
import com.mincor.viamframework.viamv2.inject.injectionresults.IInjectionResult

class Config(var request: Any, var injectionName: String) {
    var result: IInjectionResult? = null
        set(value) {
            if (field != null && value != null) {
                Log.w("InjectionConfig",
                        "Warning: Injector already has a rule for type \""
                                + this.request.toString()
                                + "\", named \""
                                + this.injectionName
                                + "\".\n "
                                + "If you have overwritten this mapping intentionally you can use "
                                + "\"injector.unmap()\" prior to your replacement mapping in order to "
                                + "avoid seeing this message.")
            }
            field = value
        }

    /**
     * Determine whether there was a response
     *
     * @param injector injector
     * @return Boolean
     */
    fun hasResponse(injector: com.mincor.viamframework.viamv2.inject.Injector): Boolean {
        if (this.result != null) return true
        val parentConfig = injector.getAncestorMapping(this.request, this.injectionName)
        return parentConfig != null
    }
}