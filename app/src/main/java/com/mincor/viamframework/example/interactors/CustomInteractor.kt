package com.mincor.viamframework.example.interactors

import com.mincor.viamframework.example.actors.UserActor
import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.components.Interactor
import com.mincor.viamframework.viam.core.Inject

class CustomInteractor : Interactor() {

    @field:Inject
    lateinit var userActor:UserActor

    @field:Inject
    lateinit var event:Event

    override fun execute() {
        println("HELLO INTERACTOR")
        when(event.type){
            EventNames.CUSTOM_REQUEST -> userActor.login()
        }
    }
}