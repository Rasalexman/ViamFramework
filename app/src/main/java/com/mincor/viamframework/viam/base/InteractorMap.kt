package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.core.*
import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viam.components.Interactor
import java.util.*
import kotlin.reflect.KClass

typealias ListenersClassMap = MutableMap<String, IListener?>
typealias EventClassMap = MutableMap<String, ListenersClassMap?>
typealias EventMutableMap = kotlin.collections.MutableMap<String, EventClassMap?>

class InteractorMap(
        /**
         * The `IEventDispatcher` to listen to
         */
        private val eventDispatcher: IEventDispatcher,
        /**
         * The `IInjector` to inject with
         */
        private val injector: IInjector,
        /**
         * The `IReflector` to reflect with
         */
        private val reflector: IReflector) : IInteractorMap {

    /**
     * Internal
     */
    private var eventTypeMap: EventMutableMap = mutableMapOf()

    /**
     * Internal
     * Collection of command classes that have been verified to implement an
     * `execute` method
     */
    private val verifiedInteractorClasses = hashMapOf<String, Any>()
    private val detainedInteractors = hashMapOf<String, Boolean>()


    override fun hold(inter: Any) {
        val key = "${inter.hashCode()}"
        this.detainedInteractors[key] = true
    }

    override fun release(inter: Any) {
        val key = "${inter.hashCode()}"
        this.detainedInteractors[key]?.let {
            this.detainedInteractors.remove(key)
        }
    }
    
    override fun execute(interactorClass: KClass<*>, payload: Any?, payloadClass: KClass<*>?, named: String) {
        this.verifyInteractorClass(interactorClass)

        if (payload != null || payloadClass != null) {
            val tempPayloadClass = payloadClass ?: this.reflector.getClass(payload!!)
            if (Event::class.isInstance(payload) && tempPayloadClass != Event::class) {
                payload?.let {
                    this.injector.mapValue(Event::class, it, "")
                }
            } else this.injector.mapValue(tempPayloadClass, payload?:Temp(), named)
        }
        val interactor = this.injector.instantiate(interactorClass) as? Interactor
        if (payload != null || payloadClass != null) {
            if (Event::class.isInstance(payload) && payloadClass != Event::class) {
                this.injector.unmap(Event::class, "")
            }
            this.injector.unmap(payloadClass, named)
        }
        interactor?.execute()
    }

    override fun mapEvent(eventName: String, interactorClass: KClass<*>, eventClass: KClass<*>?, oneShot: Boolean) {
        this.verifyInteractorClass(interactorClass)

        val tempEventClass = eventClass ?: Event::class
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: hashMapOf()

        val tempEventKey = "${tempEventClass.hashCode()}"
        val callbacksByCommandClass: ListenersClassMap = eventClassMap[tempEventKey] ?: hashMapOf()

        val interactorKey = "${interactorClass.hashCode()}"
        if (callbacksByCommandClass[interactorKey] != null)
            throw ContextError("${ContextError.E_COMMANDMAP_OVR} - eventType ( $eventName ) and Interactor ( $interactorClass )")

        val callback = MapEventListener(eventName, "callback", interactorClass, tempEventClass, oneShot)
        this.eventDispatcher.addEventListener(eventName, callback)
        callbacksByCommandClass[interactorKey] = callback
    }

    override fun unmapEvent(eventName: String, interactorClass: KClass<*>, eventClass: KClass<*>?) {
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: return
        val tempEventClass = eventClass ?: Event::class

        val eventKey = "${tempEventClass.hashCode()}"
        val callbacksByInteractorClass: ListenersClassMap = eventClassMap[eventKey] ?: return

        val interactorKey = "${interactorClass.hashCode()}"
        val callback = callbacksByInteractorClass[interactorKey] as? Listener ?: return

        this.eventDispatcher.removeEventListener(eventName, callback)  // false
        callbacksByInteractorClass.remove(interactorKey)
    }

    override fun unmapAllEvents() {

        this.eventTypeMap.forEach { eventType, _ ->
            val eventClassMap: EventClassMap? = this.eventTypeMap[eventType]
            eventClassMap?.forEach { _, oCallbacksByCommandClass ->
                val callbacksByCommandClass: ListenersClassMap? = oCallbacksByCommandClass
                callbacksByCommandClass?.forEach { _, callback ->
                    callback?.let {
                        this.eventDispatcher.removeEventListener(eventType, it)
                    }
                }
            }
        }
        this.eventTypeMap = HashMap()
    }

    override fun hasEventInteractor(eventName: String, interactorClass: KClass<*>, eventClass: KClass<*>?): Boolean {
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: return false

        val tempEventClass = eventClass ?: Event::class
        val eventKey = "${tempEventClass.hashCode()}"
        val callbacksByCommandClass: ListenersClassMap = eventClassMap[eventKey] ?: return false

        val interactorKey = "${interactorClass.hashCode()}"
        return callbacksByCommandClass[interactorKey] != null
    }

    // ---------------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------------

    /**
     * @param interactorClass interactorClass
     * throws `ContextError`
     */
    private fun verifyInteractorClass(interactorClass: KClass<*>) {
        this.verifiedInteractorClasses[interactorClass.className()]?.let {
            val key = "${interactorClass.hashCode()}"
            try {
                this.verifiedInteractorClasses[key] = interactorClass.java.getMethod("execute").toString()
            } catch (e: NoSuchMethodException) {
                println(e.message)
            }

            if (this.verifiedInteractorClasses[key] == null) {
                throw ContextError("${ContextError.E_COMMANDMAP_NOIMPL} - $interactorClass")
            }
        }
    }

    /**
     * MapEventListener Event Handler
     *
     * @param event              The `Event` Event
     * @param interactorClass    The Class to construct and execute
     * @param oneshot            Should this interactor mapping be removed after execution
     * @param originalEventClass originalEventClass
     * @return `true` if the event was routed to a Command and the
     * Command was executed, `false` otherwise
     */
    private fun routeEventToInteractor(event: Event, interactorClass: KClass<*>, oneshot: Boolean, originalEventClass: KClass<*>): Boolean? {
        if (!originalEventClass.isInstance(event))
            return false
        this.execute(interactorClass, event, null, "")
        if (oneshot) {
            this.unmapEvent(event.type, interactorClass, originalEventClass)
        }
        return true
    }


    data class Temp(val temp:String = "")


    inner class MapEventListener(override val name: String, override var type: String, private val interactorClass: KClass<*>, private val eventClass: KClass<*>, private val oneShot: Boolean = true) : Listener(name, type) {
        override fun onEventHandle(event: Event) {
            routeEventToInteractor(event, interactorClass, oneShot, eventClass)
        }
    }
}