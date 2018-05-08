package com.mincor.viamframework.viam.base.events

data class Event(val type:String, val target:Any?, val cancelable:Boolean = false)