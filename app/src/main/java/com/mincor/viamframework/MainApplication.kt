package com.mincor.viamframework

import com.mincor.viamframework.viam.views.ViamApplication

class MainApplication : ViamApplication() {

    override val viamContextInstance = MainContext(this, true)

    override fun onCreate() {
        super.onCreate()
        println("HELLO VIAM")
    }

}