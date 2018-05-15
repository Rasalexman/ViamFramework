package com.mincor.viamframework.example.actors

import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.example.model.User
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.components.Actor
import com.mincor.viamframework.viam.core.Inject

class UserActor : Actor() {

    @Inject
    lateinit var user:User

    fun login(){
        println("USER LOG IN HANDLER")
        user.name = "Alexander"
        user.sign = "admin"
        dispatch(Event(EventNames.CUSTOM_RESPOND))
    }

    fun logout(){

    }
}
