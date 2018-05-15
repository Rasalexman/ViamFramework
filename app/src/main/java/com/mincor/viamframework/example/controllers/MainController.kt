package com.mincor.viamframework.example.controllers

import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.example.model.User
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.components.ViewController
import com.mincor.viamframework.viam.components.mapEvent
import com.mincor.viamframework.viam.core.Inject
import com.mincor.viamframework.viam.core.PostConstruct

class MainController : ViewController() {

    @field:Inject
    lateinit var user:User

    override fun onAttach() {
        println("HELLO MAIN CONTROLLER")

        mapEvent(EventNames.CUSTOM_RESPOND, {
            println("HELLO FROM MAIN CONTROLLER ${user.name}")
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