package com.mincor.viamframework.viam.core

interface IViewControllerMap : IContextViewHolder {

    fun mapView(view:Any, controller:Class<out IViewController>, injectViewAs:Any?, autoCreate:Boolean = true, autoRemove:Boolean = true)

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