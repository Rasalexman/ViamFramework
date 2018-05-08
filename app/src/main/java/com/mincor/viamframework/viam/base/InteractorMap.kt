package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.core.*
import com.mincor.viamframework.viam.base.events.Event


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


    override fun execute(interactorClass: Class<*>, payload: Any?, payloadClass: Class<*>?, named: String) {
        this.verifyInteractorClass(interactorClass)

        if (payload != null || payloadClass != null) {
            val tempPayloadClass = payloadClass ?: this.reflector?.getClass(payload)
            if (Event::class.java.isInstance(payload) && tempPayloadClass != Event::class.java) {
                this.injector?.mapValue(Event::class.java, payload, "")
            }
            this.injector?.mapValue(tempPayloadClass, payload, named)
        }
        val command = this.injector?.instantiate(interactorClass)
        if (payload != null || payloadClass != null) {
            if (Event::class.java.isInstance(payload) && payloadClass != Event::class.java) {
                this.injector?.unmap(Event::class.java, "")
            }
            this.injector?.unmap(payloadClass, named)
        }
        // TODO: MAKE COMMAND IMPLEMENTATION
       // (command as Command).execute()
    }

    override fun mapEvent(eventName: String, interactorClass: Class<*>, eventClass: Class<*>?, oneShot: Boolean) {
        this.verifyInteractorClass(interactorClass)

        val tempEventClass = eventClass ?: Event::class.java
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: hashMapOf()

        val callbacksByCommandClass: ListenersClassMap = eventClassMap["${tempEventClass.hashCode()}"]
                ?: hashMapOf()

        if (callbacksByCommandClass["${interactorClass.hashCode()}"] != null)
            throw ContextError("${ContextError.E_COMMANDMAP_OVR} - eventType ( $eventName ) and Command ( $interactorClass )")

        val callback = MapEventListener(eventName, "callback", interactorClass, tempEventClass, oneShot)
        this.eventDispatcher.addEventListener(eventName, callback) // TODO: ..., false, 0, true)
        callbacksByCommandClass["${interactorClass.hashCode()}"] = callback
    }

    override fun unmapEvent(eventName: String, interactorClass: Class<*>, eventClass: Class<*>?) {
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
                    this.eventDispatcher.removeEventListener(eventType, callback)
                }
            }
        }
        this.eventTypeMap = HashMap()
    }

    override fun hasEventInteractor(eventName: String, interactorClass: Class<*>, eventClass: Class<*>?): Boolean {
        val eventClassMap: EventClassMap = this.eventTypeMap[eventName] ?: return false

        val tempEventClass = eventClass ?: Event::class.java
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
    protected fun verifyInteractorClass(commandClass: Class<*>) {
        this.verifiedInteractorClasses[commandClass.name]?.let {
            val key = "${commandClass.hashCode()}"
            try {
                this.verifiedInteractorClasses[key] = commandClass.getMethod("execute").toString()
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
     * @param oneshot            Should this command mapping be removed after execution?
     * @param originalEventClass originalEventClass
     * @return `true` if the event was routed to a Command and the
     * Command was executed, `false` otherwise
     */
    protected fun routeEventToCommand(event: Event, commandClass: Class<*>, oneshot: Boolean?, originalEventClass: Class<*>): Boolean? {
        if (!originalEventClass.isInstance(event))
            return false

        //this.execute(commandClass, event, null, "")
        if (oneshot!!) {
            //this.unmapEvent(event.type, commandClass, originalEventClass)
        }
        return true
    }


    inner class MapEventListener(override val name: String, override var type: String, private val interactorClass: Class<*>, private val eventClass: Class<*>, private val oneShot: Boolean) : Listener(name, type) {
        override fun onEventHandle(event: Event) {
            routeEventToCommand(event, interactorClass, oneShot, eventClass)
        }
    }
}