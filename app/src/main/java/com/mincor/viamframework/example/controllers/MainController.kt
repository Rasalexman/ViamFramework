package com.mincor.viamframework.example.controllers

import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.example.model.User
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.components.ViewController
import com.mincor.viamframework.viam.core.Inject

class MainController : ViewController() {

    @field:Inject
    lateinit var user:User

    override fun onAttach() {
        println("HELLO MAIN CONTROLLER")

        eventMap.mapListener(eventDispatcher, EventNames.CUSTOM_RESPOND, {
            println("CONTROLLER HANDLE ${user.name}")
        })

        dispatch(Event(EventNames.CUSTOM_REQUEST))
    }

    override fun onDetach() {

    }
}