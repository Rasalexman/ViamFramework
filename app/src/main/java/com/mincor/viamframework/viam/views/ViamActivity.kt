package com.mincor.viamframework.viam.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.mincor.viamframework.viam.base.events.EventDispatcher
import com.mincor.viamframework.viam.base.ext.className

abstract class ViamActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * inject the activity's controller when it on create
         */
        try {
            (this.application as IApplication).viamContext.injectViewController(this)
            EventDispatcher.setDispatcher("${this::class.className()}${this.hashCode()}")
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Please create a custom ViamApplication and fill in the getViamContextInstance() method")
        }

    }

    override fun onDestroy() {
        try {
            EventDispatcher.removeDispatcher("${this::class.className()}${this.hashCode()}")
            (this.application as IApplication).viamContext.removeViewController(this)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Please create a custom ViamApplication and fill in the getViamContextInstance() method")
        }

        super.onDestroy()
    }

}
