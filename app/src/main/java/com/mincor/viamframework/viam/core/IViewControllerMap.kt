package com.mincor.viamframework.viam.core

interface IViewControllerMap : IContentViewHolder {

    fun mapView(view:Any, controller:IViewController, injectViewAs:Any?, autoCreate:Boolean = false, autoRemove:Boolean = false)

    fun unmapView(view:Any)

    fun createController(viewComponent: Any): IViewController

    fun registerController(viewComponent: Any, viewController:IViewController)

    fun removeController(mediator: IViewController): IViewController

    fun unInjectController(viewComponent: Any)

    fun removeControllerByView(viewComponent: Any): IViewController

    fun retrieveController(viewComponent: Any): IViewController

    fun hasMapping(viewClassOrName: Any): Boolean

    fun hasController(mediator: IViewController): Boolean

    fun hasControllerForView(viewComponent: Any): Boolean?

    fun addController(viewComponent: Any)

}