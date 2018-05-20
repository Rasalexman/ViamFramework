package com.mincor.viamframework

import android.app.Application
import com.mincor.viamframework.viamv2.IViamApplication

class MainApplication : Application() , IViamApplication {

   override val viamContext = MyViamContext(this)

    override fun onCreate() {
        super.onCreate()
        println("HELLO VIAM")
    }

}