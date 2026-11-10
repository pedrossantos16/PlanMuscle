package com.pedro.planmuscle.di

import android.content.Context
import com.pedro.planmuscle.data.local.PlanMuscleDatabase
import com.pedro.planmuscle.data.preferences.UserPreferencesDataStore
import com.pedro.planmuscle.data.repository.PlanMuscleRepositoryImpl
import com.pedro.planmuscle.domain.repository.PlanMuscleRepository

class AppContainer(private val context: Context) {

    val database: PlanMuscleDatabase by lazy {
        PlanMuscleDatabase.getDatabase(context)
    }

    val repository: PlanMuscleRepository by lazy {
        PlanMuscleRepositoryImpl(database)
    }

    val userPreferencesDataStore: UserPreferencesDataStore by lazy {
        UserPreferencesDataStore(context)
    }
}
