package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.core.IInjector
import com.mincor.viamframework.viam.core.IReflector
import com.mincor.viamframework.viam.core.IViewController
import com.mincor.viamframework.viam.core.IViewControllerMap
import kotlin.reflect.KClass

class ViewControllerMap(private val reflector: IReflector, override var contextView: Any?, injector: IInjector) : ViewMapBase(injector), IViewControllerMap {

    private var controllerByView = hashMapOf<String, IViewController?>()
    private var mappingConfigByView = hashMapOf<String, MappingConfig?>()
    private var mappingConfigByViewClassName = hashMapOf<String, MappingConfig?>()
    private var controllerMarkedForRemoval = hashMapOf<String, Any>()
    private var hasControllerMarkedForRemoval: Boolean = false

    override var isEnabled: Boolean = true

    override fun mapView(view: KClass<*>, controller: KClass<out IViewController>, injectViewAs: Any?, autoCreate: Boolean, autoRemove: Boolean) {
        val viewClassName = this.reflector.getFullyQualifiedClassName(view, false)
        if (this.mappingConfigByViewClassName[viewClassName] != null) {
            throw ContextError("${ContextError.E_MEDIATORMAP_OVR} - $controller")
        }

        if (!this.reflector.classExtendsOrImplements(controller, IViewController::class)) {
            throw ContextError("${ContextError.E_MEDIATORMAP_NOIMPL} - $controller")
        }

        val config = MappingConfig(controller, null, autoCreate, autoRemove)
        injectViewAs?.let {
            when (it) {
                is List<*> -> config.typedViewClasses = (injectViewAs as? ArrayList<KClass<*>>)?.clone() as? ArrayList<KClass<*>>
                is KClass<*> -> config.typedViewClasses = arrayListOf(injectViewAs as KClass<*>)
            }
        } ?: let {
            config.typedViewClasses = arrayListOf(view)
        }

        this.mappingConfigByViewClassName[viewClassName] = config

        if (autoCreate || autoRemove) {
            this.viewListenerCount++
            if (this.viewListenerCount == 1) {
                this.addListeners()
            }
        }

        /*
         * This was a bad idea - causes unexpected eager instantiation of object
         * graph
         */
        this.contextView?.let {
            if (autoCreate && viewClassName == it::class.qualifiedName) {
                this.createControllerUsing(view, viewClassName, config)
            }
        }
    }

    override fun unmapView(view: Any) {
        val viewClassName = this.reflector.getFullyQualifiedClassName(view, false)
        val config = this.mappingConfigByViewClassName[viewClassName]
        config?.let {
            if (it.autoCreate || it.autoRemove) {
                this.viewListenerCount--
                if (this.viewListenerCount == 0) {
                    this.removeListeners()
                }
            }
        }
        this.mappingConfigByViewClassName.remove(viewClassName)
    }

    override fun hasMapping(viewClassOrName: Any): Boolean {
        val tempClassName = if (viewClassOrName == "") viewClassOrName.javaClass.name else viewClassOrName
        return this.mappingConfigByViewClassName[tempClassName] != null
    }

    override fun createController(viewComponent: Any): IViewController? =
            this.createControllerUsing(viewComponent, "", null)

    override fun registerController(viewComponent: Any, viewController: IViewController) {
        val controllerClass = this.reflector.getClass(viewController)
        if (this.injector.hasMapping(controllerClass, "")) {
            this.injector.unmap(controllerClass, "")
        }

        val viewKey = "${viewComponent.hashCode()}"
        this.injector.mapValue(controllerClass, viewController, "")
        this.controllerByView[viewKey] = viewController
        this.mappingConfigByView[viewKey] = this.mappingConfigByViewClassName[viewComponent.javaClass.name]
        viewController.view = viewComponent
        viewController.preAttach()
    }

    override fun addController(viewComponent: Any) {
        this.onViewAdded(viewComponent)
    }

    override fun removeController(controller: IViewController?): IViewController? {
        controller?.let {
            val viewComponent = controller.view
            val mediatorClass = this.reflector.getClass(controller)

            viewComponent?.let {
                val viewKey = "${it.hashCode()}"
                this.controllerByView.remove(viewKey)
                this.mappingConfigByView.remove(viewKey)
                controller.preDetach()
                controller.view = null
                if (this.injector.hasMapping(mediatorClass, "")) {
                    this.injector.unmap(mediatorClass, "")
                }
            }
        }

        return controller
    }

    override fun removeControllerByView(viewComponent: Any): IViewController? = this.removeController(this.retrieveController(viewComponent))
    override fun retrieveController(viewComponent: Any): IViewController? = this.controllerByView["${viewComponent.hashCode()}"]
    override fun hasController(mediator: IViewController): Boolean = this.controllerByView.any { it.hashCode() == mediator.hashCode() }
    override fun hasControllerForView(viewComponent: Any): Boolean = retrieveController(viewComponent) != null

    override fun unInjectController(viewComponent: Any) {
        this.onViewRemoved(viewComponent)
    }

    /**
     * When the Controller to create or are using
     *
     * @param viewComponent viewComponent
     * @param viewClassName viewClassName
     * @param config        config
     * @return IViewController
     */
    protected fun createControllerUsing(viewComponent: Any, viewClassName: String, config: MappingConfig?): IViewController? {

        val viewKey = "${viewComponent.hashCode()}"
        var controller: IViewController? = this.controllerByView[viewKey]
        controller ?: let {
            val tempClassName = if (viewClassName == "") viewComponent.javaClass.name else viewClassName
            val tempConfig = config ?: this.mappingConfigByViewClassName[tempClassName]
            tempConfig?.let {

                it.typedViewClasses?.forEach {
                    this.injector.mapValue(it, viewComponent, "")
                }

                controller = this.injector.instantiate(it.controllerClass) as IViewController

                it.typedViewClasses?.forEach {
                    this.injector.unmap(it, "")
                }
                this.registerController(viewComponent, controller!!)
            }
        }

        return controller
    }

    /**
     * When the view is added
     *
     * @param e view
     */
    override fun onViewAdded(e: Any) {
        val viewKey = "${e.hashCode()}"
        if (this.controllerMarkedForRemoval[viewKey] != null) {
            this.controllerMarkedForRemoval.remove(viewKey)
            return
        }
        val viewClassName = e::class.qualifiedName?:""
        val config = this.mappingConfigByViewClassName[viewClassName]
        config?.let {
            if (it.autoCreate) {
                this.createControllerUsing(e, viewClassName, config)
            }
        }
    }

    protected fun onViewRemoved(view: Any) {
        val viewKey = "${view.hashCode()}"
        val config = this.mappingConfigByView[viewKey]
        config?.let {
            if(it.autoRemove){
                this.controllerMarkedForRemoval[viewKey] = view
                if(!hasControllerMarkedForRemoval) {
                    hasControllerMarkedForRemoval = true
                    this.removeControllerLater()
                }
            }
        }
    }

    /**
     * Flex framework work-around part #6
     * When remove the Mediator later
     */
    protected fun removeControllerLater() {
        for (view in this.controllerMarkedForRemoval.values) {
            this.removeControllerByView(view)
            this.controllerMarkedForRemoval.remove("${view.hashCode()}")
        }
        this.hasControllerMarkedForRemoval = false
    }

    override fun addListeners() {

    }

    override fun removeListeners() {

    }

    data class MappingConfig(
            var controllerClass: KClass<*>,
            var typedViewClasses: List<KClass<*>>? = null,
            var autoCreate: Boolean = false,
            var autoRemove: Boolean = false
    )
}