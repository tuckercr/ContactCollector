package com.ns4d.contactCollector

import android.app.Application
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager

/**
 * Application class - starts and stops DBFlow
 *
 * Created by ctucker on 6/22/17.
 */
@Suppress("unused")
class CollectorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FlowManager.init(FlowConfig.Builder(this).build())
    }

    override fun onTerminate() {
        super.onTerminate()
        FlowManager.destroy()
    }
}
