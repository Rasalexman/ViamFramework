package com.mincor.viamframework.viam.core

import kotlin.reflect.KClass

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
    fun execute(interactorClass: KClass<*>, payload: Any?, payloadClass:KClass<*>?, named:String)


    /**
     * Map event to selected Interactor
     */
    fun mapEvent(eventName:String, interactorClass: KClass<*>, eventClass: KClass<*>?, oneShot:Boolean = false)

    /**
     * Unmap event from selected Interactor
     */
    fun unmapEvent(eventName:String, interactorClass: KClass<*>, eventClass: KClass<*>?)

    /**
     * Unmap all events from selected Interactor
     */
    fun unmapAllEvents()


    /**
     * Check if a interactor has event
     */
    fun hasEventInteractor(eventName:String, interactorClass: KClass<*>, eventClass: KClass<*>?):Boolean
}