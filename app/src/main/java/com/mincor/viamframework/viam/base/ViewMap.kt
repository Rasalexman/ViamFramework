package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.base.events.Event
import com.mincor.viamframework.viam.base.events.EventDispatcher
import com.mincor.viamframework.viam.base.events.EventTypes
import com.mincor.viamframework.viam.core.IInjector
import com.mincor.viamframework.viam.core.IViewMap
import java.util.*
import kotlin.reflect.KClass


class ViewMap(viewContext:Any, injector:IInjector) : ViewMapBase(injector), IViewMap {

    init {
        contextView = viewContext
    }

    override var isEnabled: Boolean = true
        set(value) {
            if (value != field) {
                this.removeListeners()
                field = value
                if (this.viewListenerCount > 0) {
                    this.addListeners()
                }
            }
        }

    /**
     * private
     */
    protected val mappedPackages = arrayListOf<String>()

    /**
     * private
     */
    protected val mappedTypes = hashMapOf<String, KClass<*>>()

    /**
     * private
     */
    protected val injectedViews = WeakHashMap<String, Any?>()

    override fun mapPackage(packageName: String) {
        if (this.mappedPackages.indexOf(packageName) == -1) {
            this.mappedPackages.add(packageName)
            this.viewListenerCount++
            if (this.viewListenerCount == 1) {
                this.addListeners()
            }
        }
    }

    override fun unmapPackage(packageName: String) {
        val index = this.mappedPackages.indexOf(packageName)
        if (index > -1) {
            this.mappedPackages.removeAt(index)
            this.viewListenerCount--
            if (this.viewListenerCount == 0) {
                this.removeListeners()
            }
        }
    }

    override fun hasPackage(packageName: String): Boolean = this.mappedPackages.indexOf(packageName) > -1


    override fun mapType(type: KClass<*>) {
        val typeKey = "${type.hashCode()}"
        if (this.mappedTypes[typeKey] != null)
            return

        this.mappedTypes[typeKey] = type
        this.viewListenerCount++
        if (this.viewListenerCount == 1) {
            this.addListeners()
        }

        contextView?.let {
            if(type.isInstance(it)) {
                this.injectInto(it)
            }
        }
    }

    override fun unmapType(type: KClass<*>) {
        val typeKey = "${type.hashCode()}"
        val mapping = this.mappedTypes[typeKey]
        mapping?.let {
            this.mappedTypes.remove(typeKey)
            this.viewListenerCount--
            if (this.viewListenerCount == 0) {
                this.removeListeners()
            }
        }
    }

    override fun hasType(type: KClass<*>): Boolean = this.mappedTypes["${type.hashCode()}"] != null


    /**
     * inject the contextView
     * Then save the hashCode joining injectedViews
     *
     * @param target The ContextView
     */
    protected fun injectInto(target: Any) {
        this.injector.injectInto(target)
        this.injectedViews["${target.hashCode()}"] = true
    }


    ///------- LISTENER OVERRIDES
    override fun addListeners() {
        this.contextView?.let {
            if(this.isEnabled){
                val contentKey = "${it.hashCode()}"
                val dispatcher = EventDispatcher.setDispatcher(contentKey)
                dispatcher.addEventListener(EventTypes.ADDED_TO_STAGE, ViewAddedListener(EventTypes.ADDED_TO_STAGE,
                        "onViewAdded"))
            }
        }
    }

    override fun removeListeners() {
        this.contextView?.let {
            val contentKey = "${it.hashCode()}"
            val dispatcher = EventDispatcher.removeDispatcher(contentKey)
            dispatcher?.removeEventListener(EventTypes.ADDED_TO_STAGE, ViewAddedListener(EventTypes.ADDED_TO_STAGE,
                    "onViewAdded"))
        }
    }

    override fun onViewAdded(e: Any) {

        if(e is Event){
            val target = e.target
            target?.let {
                val targetKey = "${it.hashCode()}"
                if (this.injectedViews[targetKey] != null)
                    return

                this.mappedTypes.forEach { _, clazz ->
                    if (clazz.isInstance(target)) {
                        this.injectInto(target)
                        return@forEach
                    }
                }

                val len = this.mappedPackages.size
                if (len > 0) {
                    val className = it.javaClass.name
                    for (i in 0 until len) {
                        val packageName = this.mappedPackages[i]
                        if (className.indexOf(packageName) == 0) {
                            this.injectInto(target)
                            return
                        }
                    }
                }
            }
        }


    }

    /**
     * The listener listens for whether the view is added
     */
    private inner class ViewAddedListener(type: String, name: String) : Listener(type, name) {
        override fun onEventHandle(event: Event) {
            this@ViewMap.onViewAdded(event)
        }
    }
}