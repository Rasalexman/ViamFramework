package com.mincor.viamframework.viam

import com.mincor.viamframework.MainActivity
import com.mincor.viamframework.example.controllers.MainController
import com.mincor.viamframework.example.actors.UserActor
import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.example.interactors.CustomInteractor
import com.mincor.viamframework.example.model.User
import com.mincor.viamframework.viam.components.Context

class MainContext(context: Any, autoStartUp:Boolean = true) : Context(context, autoStartUp) {

    override fun setRelation() {
        injector.mapSigleton(UserActor::class.java, "")
        injector.mapSigleton(User::class.java, "")
        interactorMap.mapEvent(EventNames.CUSTOM_REQUEST, CustomInteractor::class.java, null, true)
        viewControllerMap.mapView(MainActivity::class.java, MainController::class.java, null)
    }
}