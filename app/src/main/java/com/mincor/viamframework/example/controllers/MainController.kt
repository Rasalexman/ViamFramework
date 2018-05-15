package com.mincor.viamframework.example.controllers

import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.example.model.User
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.components.ViewController
import com.mincor.viamframework.viam.core.Inject
import com.mincor.viamframework.viam.core.PostConstruct
import kotlin.properties.Delegates

class MainController : ViewController() {

    @Inject
    lateinit var user:User

    @PostConstruct
    var callLaterHandler = ::takeAdvantageHandler

    val myUser:User by Delegates.observable(User(), { prop, old, fresh->
        println("INJECT COMPLETE $prop -> $old  -> $fresh")
    })

    override fun onAttach() {
        println("HELLO MAIN CONTROLLER")

        eventMap.mapListener(eventDispatcher, EventNames.CUSTOM_RESPOND, {
            println("CONTROLLER HANDLE ${user.name}")
        })

        dispatch(Event(EventNames.CUSTOM_REQUEST))
    }

    @PostConstruct
    fun takeAdvantageHandler(){
        println("THIS IS A POST CONSTRUCT METHOD CALL")
    }

    override fun onDetach() {

    }
}