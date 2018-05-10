package com.mincor.viamframework

import android.os.Bundle
import com.mincor.viamframework.viam.views.ViamActivity

class MainActivity : ViamActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
