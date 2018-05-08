package com.mincor.viamframework.viam.core

import com.mincor.viamframework.viam.base.Event

interface IListener {
    val name:String
    var type:String

    fun onEventHandle(event:Event)
}