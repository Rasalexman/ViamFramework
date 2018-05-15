package com.mincor.viamframework

import com.mincor.viamframework.MainActivity
import com.mincor.viamframework.example.controllers.MainController
import com.mincor.viamframework.example.actors.UserActor
import com.mincor.viamframework.example.common.EventNames
import com.mincor.viamframework.example.interactors.CustomInteractor
import com.mincor.viamframework.example.model.User
import com.mincor.viamframework.viam.components.Context

class MainContext(context: Any, autoStartUp:Boolean = true) : Context(context, autoStartUp) {

    override fun setRelation() {
        injector.mapSigleton(UserActor::class, "")
        injector.mapSigleton(User::class, "")
        interactorMap.mapEvent(EventNames.CUSTOM_REQUEST, CustomInteractor::class, null, true)
        viewControllerMap.mapView(MainActivity::class, MainController::class, null)
    }
}