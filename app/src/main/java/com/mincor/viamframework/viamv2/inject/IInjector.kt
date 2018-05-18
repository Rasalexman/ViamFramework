package com.mincor.viamframework.viamv2.inject

import com.mincor.viamframework.viamv2.inject.data.Config
import com.mincor.viamframework.viamv2.inject.data.Description

interface IInjector {
    val mappings: MutableMap<String, Config>
    val injectedDescriptions: MutableMap<String, Description?>
    var attendedToInjectees: MutableMap<String, Boolean>
    var parentInjector: IInjector?
}