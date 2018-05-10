package com.mincor.viamframework

import com.mincor.viamframework.viam.MainContext
import com.mincor.viamframework.viam.components.Context
import com.mincor.viamframework.viam.views.ViamApplication

class MainApplication : ViamApplication() {

    override val mvcContextInstance: Context = MainContext(this, true)

    override fun onCreate() {
        super.onCreate()
        println("HELLO VIAM")
    }

}