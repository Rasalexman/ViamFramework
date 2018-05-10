package com.mincor.viamframework.viam.base.events

import android.view.View

data class Event(val type:String, val target:View? = null, val cancelable:Boolean = false)