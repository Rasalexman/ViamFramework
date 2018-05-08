package com.mincor.viamframework.viam.core

interface IInteractorMap {

    /**
     * Hold on Interactor instance
     * @param inter - the interactor to hold on
     */
    fun hold(inter:Any)


    /**
     * Release a interactor instance
     * @param inter - the interactor to release
     */
    fun release(inter:Any)

    /**
     *
     */
    fun execute(interactorClass: Class<*>, payload: Any?, payloadClass:Class<*>?, named:String)


    /**
     * Map event to selected Interactor
     */
    fun mapEvent(eventName:String, interactorClass: Class<*>, eventClass: Class<*>?, oneShot:Boolean = false)

    /**
     * Unmap event from selected Interactor
     */
    fun unmapEvent(eventName:String, interactorClass: Class<*>, eventClass: Class<*>?)

    /**
     * Unmap all events from selected Interactor
     */
    fun unmapAllEvents()


    /**
     * Check if a interactor has event
     */
    fun hasEventInteractor(eventName:String, interactorClass: Class<*>, eventClass: Class<*>?):Boolean
}