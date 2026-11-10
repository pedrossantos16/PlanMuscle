package com.pedro.planmuscle

import android.app.Application
import com.pedro.planmuscle.di.AppContainer

class PlanMuscleApplication : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
