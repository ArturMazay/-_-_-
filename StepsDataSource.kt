package ru.korpu.android.domain.stepsRepository

import kotlinx.coroutines.flow.Flow

interface StepsDataSource {
    fun getSteps(): Flow<Int>
}