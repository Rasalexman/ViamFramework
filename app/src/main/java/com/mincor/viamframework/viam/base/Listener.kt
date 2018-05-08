package com.mincor.viamframework.viam.base

import com.mincor.viamframework.viam.core.IListener

abstract class Listener(override val name: String = "", override var type: String = "") : IListener