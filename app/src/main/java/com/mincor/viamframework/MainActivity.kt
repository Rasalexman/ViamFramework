package com.mincor.viamframework

import android.app.Activity
import android.os.Bundle
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.core.IEventDispatcher
import com.mincor.viamframework.viamv2.inject.inject

class MainActivity : Activity(), com.mincor.viamframework.viamv2.inject.IInjector {

    val testContent:IEventDispatcher by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testContent.dispatchEvent(Event("HELLO"))
    }
}
