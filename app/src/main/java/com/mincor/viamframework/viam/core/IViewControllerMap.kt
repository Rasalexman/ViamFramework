package com.mincor.viamframework.viam.core

import kotlin.reflect.KClass

interface IViewControllerMap : IContextViewHolder {

    fun mapView(view:KClass<*>, controller: KClass<out IViewController>, injectViewAs:Any?, autoCreate:Boolean = true, autoRemove:Boolean = true)

    fun unmapView(view:Any)

    fun createController(viewComponent: Any): IViewController?

    fun registerController(viewComponent: Any, viewController:IViewController)

    fun removeController(controller: IViewController?): IViewController?

    fun unInjectController(viewComponent: Any)

    fun removeControllerByView(viewComponent: Any): IViewController?

    fun retrieveController(viewComponent: Any): IViewController?

    fun hasMapping(viewClassOrName: Any): Boolean

    fun hasController(mediator: IViewController): Boolean

    fun hasControllerForView(viewComponent: Any): Boolean

    fun addController(viewComponent: Any)

}