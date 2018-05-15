package com.mincor.viamframework.viam.views

import android.app.Application
import com.mincor.viamframework.viam.components.Context

abstract class ViamApplication : Application(), IApplication {

    /**
     * Get the viam's context
     */
    override val viamContext: Context by lazy {
        this.viamContextInstance
    }

    /**
     * Please write your custom viam context
     * TODO After write your custom viam context
     *
     * @return Context
     */
    protected abstract val viamContextInstance: Context

    override fun onCreate() {
        if(!viamContext.autoStartup) viamContext.startup()
        super.onCreate()
    }
}
