package com.mincor.viamframework.viam.views

import android.app.Application
import com.mincor.viamframework.viam.components.Context

abstract class ViamApplication : Application(), IApplication {

    var context: Context? = null

    /**
     * Get the viam's context
     */
    override val mvcContext: Context
        get() {
            if (this.context == null)
                this.context = this.mvcContextInstance

            return this.context!!
        }

    /**
     * Please write your custom viam context
     * TODO After write your custom viam context, please don't call this method
     *
     * @return Context
     */
    protected abstract val mvcContextInstance: Context

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    override fun onCreate() {
        super.onCreate()
        this.mvcContext
    }

}
