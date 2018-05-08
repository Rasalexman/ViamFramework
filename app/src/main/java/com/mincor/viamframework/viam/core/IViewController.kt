package com.mincor.viamframework.viam.core

interface IViewController {

    var view:Any?

    fun preAttach()

    fun onAttach()

    fun preDetach()

    fun onDetach()
}