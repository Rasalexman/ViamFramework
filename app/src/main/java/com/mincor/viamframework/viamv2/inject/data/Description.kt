package com.mincor.viamframework.viamv2.inject.data

import com.mincor.viamframework.viamv2.inject.injectionpoints.InjectionPoint

data class Description(var ctor: InjectionPoint? = null, var injectionPoints: List<InjectionPoint>? = null)
