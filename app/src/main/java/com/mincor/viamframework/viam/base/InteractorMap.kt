package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.core.*
import com.mincor.viamframework.viam.base.events.Event
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
        private val injector: IInjector? = null,
        /**
         * The `IReflector` to reflect with
         */
        private val reflector: IReflector? = null) : IInteractorMap {

    /**
     * Internal
     */
    protected var eventTypeMap: EventMutableMap = mutableMapOf()

    /**
     * Internal
     * Collection of command classes that have been verified to implement an
     * `execute` method
     */
    protected val verifiedInteractorClasses = hashMapOf<String, Any>()
    protected val detainedInteractors = hashMapOf<String, Boolean>()


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
            val tempPayloadClass = payloadClass ?: this.reflector?.getClass(payload!!)
            if (Event::class.java.isInstance(payload) && tempPayloadClass != Event::class.java) {
                payload?.let {
                    this.injector?.mapValue(Event::class, it, "")
                }
            }
            this.injector?.mapValue(tempPayloadClass, payload?:Temp(), named)
        }
        val interactor = this.injector?.instantiate(interactorClass) as? Interactor
        if (payload != null || payloadClass != null) {
            if (Event::class.java.isInstance(payload) && payloadClass != Event::class.java) {
                this.injector?.unmap(Event::class, "")
            }
            this.injector?.unmap(payloadClass, named)
        }
        interactor?.execute()
    }

    override fun mapEvent(eventName: String, interactorClass: KClass<*>, eventClass: KClass<*>?, oneShot: Boolean) {
        this.verifyInteractorClass(interactorClass)

        val tempEventClass = eventClass ?: Event::class
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: hashMapOf()

        val callbacksByCommandClass: ListenersClassMap = eventClassMap["${tempEventClass.hashCode()}"]
                ?: hashMapOf()

        val interactorKey = "${interactorClass.hashCode()}"
        if (callbacksByCommandClass[interactorKey] != null)
            throw ContextError("${ContextError.E_COMMANDMAP_OVR} - eventType ( $eventName ) and Command ( $interactorClass )")

        val callback = MapEventListener(eventName, "callback", interactorClass, tempEventClass, oneShot)
        this.eventDispatcher.addEventListener(eventName, callback) // TODO: ..., false, 0, true)
        callbacksByCommandClass[interactorKey] = callback
    }

    override fun unmapEvent(eventName: String, interactorClass: KClass<*>, eventClass: KClass<*>?) {
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: return
        val tempEventClass = eventClass ?: Event::class.java

        val eventKey = "${tempEventClass.hashCode()}"
        val callbacksByCommandClass: ListenersClassMap = eventClassMap[eventKey] ?: return

        val interactorKey = "${interactorClass.hashCode()}"
        val callback = callbacksByCommandClass[interactorKey] as? Listener ?: return

        this.eventDispatcher.removeEventListener(eventName, callback)  // false
        callbacksByCommandClass.remove(interactorKey)
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
     * @param commandClass commandClass
     * throws `ContextError`
     */
    protected fun verifyInteractorClass(commandClass: KClass<*>) {
        this.verifiedInteractorClasses[commandClass.java.name]?.let {
            val key = "${commandClass.hashCode()}"
            try {
                this.verifiedInteractorClasses[key] = commandClass.java.getMethod("execute").toString()
            } catch (e: NoSuchMethodException) {
                println(e.message)
            }

            if (this.verifiedInteractorClasses[key] == null) {
                throw ContextError("${ContextError.E_COMMANDMAP_NOIMPL} - $commandClass")
            }
        }
    }

    /**
     * MapEventListener Event Handler
     *
     * @param event              The `Event` Event
     * @param commandClass       The Class to construct and execute
     * @param oneshot            Should this command mapping be removed after execution
     * @param originalEventClass originalEventClass
     * @return `true` if the event was routed to a Command and the
     * Command was executed, `false` otherwise
     */
    protected fun routeEventToCommand(event: Event, commandClass: KClass<*>, oneshot: Boolean, originalEventClass: KClass<*>): Boolean? {
        if (!originalEventClass.isInstance(event))
            return false
        this.execute(commandClass, event, null, "")
        if (oneshot) {
            this.unmapEvent(event.type, commandClass, originalEventClass)
        }
        return true
    }


    data class Temp(val temp:String = "")


    inner class MapEventListener(override val name: String, override var type: String, private val interactorClass: KClass<*>, private val eventClass: KClass<*>, private val oneShot: Boolean = true) : Listener(name, type) {
        override fun onEventHandle(event: Event) {
            routeEventToCommand(event, interactorClass, oneShot, eventClass)
        }
    }
}