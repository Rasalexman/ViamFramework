package com.mincor.viamframework.viam.injection

import com.mincor.viamframework.viam.base.ext.className
import com.mincor.viamframework.viam.base.prototypes.XML
import com.mincor.viamframework.viam.components.Actor
import com.mincor.viamframework.viam.components.Interactor
import com.mincor.viamframework.viam.components.ViewController
import com.mincor.viamframework.viam.core.IInjector


class SuspendInjector(xmlConfig: XML?) : Injector(getConstructParam(xmlConfig)), IInjector {

    companion object {

        private val XML_CONFIG = initXML_CONFIG()

        /**
         * Init the XML config
         *
         * @return XML
         */
        private fun initXML_CONFIG(): XML {
            val result = XML()
            result.setName("types")
                    .appendChild(
                            XML()
                                    .setName("type")
                                    .setValue("name", Interactor::class.className())
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "eventDispatcher")))
                    .appendChild(
                            XML()
                                    .setName("type")
                                    .setValue("name", Actor::class.className())
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "contextView"))
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "mediatorMap"))
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "eventDispatcher"))
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "injector"))
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "commandMap")))
                    .appendChild(
                            XML()
                                    .setName("type")
                                    .setValue("name", ViewController::class.className())
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "contextView"))
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "mediatorMap"))
                                    .appendChild(
                                            XML().setName("field").setValue(
                                                    "name", "eventDispatcher")))
            return result
        }

        /**
         * Get the parameters if constructor
         *
         * @param xmlConfig xmlConfig
         * @return XML
         */
        private fun getConstructParam(xmlConfig: XML?): XML? {
            if (xmlConfig != null) {
                for (typeNode in XML_CONFIG.children()) {
                    xmlConfig.appendChild(typeNode)
                }
            }
            return xmlConfig
        }
    }

    override fun createChild(): IInjector {
        val injector = SuspendInjector(null)
        injector.parentInjector = this
        injector.createChildInjector()
        return injector
    }
}