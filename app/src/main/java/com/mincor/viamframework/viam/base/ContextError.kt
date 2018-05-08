package com.mincor.viamframework.viam.base

class ContextError(messageInput:String = "") : Error(messageInput) {

    companion object {
        /***********************************
         * Context
         */
        const val E_CONTEXT_VIEW_OVR = "Context contextView must only be set once"

        const val E_CONTEXT_INJECTOR = "The ContextBase does not specify a concrete IInjector. Please override the injector getter in your concrete or abstract Context."

        const val E_CONTEXT_REFLECTOR = "The ContextBase does not specify a concrete IReflector. Please override the reflector getter in your concrete or abstract Context."

        /**********************************
         * EventMap
         */

        const val E_EVENTMAP_NOSNOOPING = "Listening to the context eventDispatcher is not enabled for this EventMap"

        /*********************************
         * CommandMap
         */

        const val E_COMMANDMAP_OVR = "Cannot overwrite map"

        const val E_COMMANDMAP_NOIMPL = "Command Class does not implement an execute() method"

        /*********************************
         * MediatorMap
         */

        const val E_MEDIATORMAP_OVR = "Mediator Class has already been mapped to a View Class in this context"

        const val E_MEDIATORMAP_NOIMPL = "Mediator Class does not implement IMediator"

        /************************************************************************************/
    }

}