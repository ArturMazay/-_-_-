package ru.korpu.android.data.stepsDataSource

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import ru.korpu.android.domain.editProfileRepository.EditProfileDataSource
import ru.korpu.android.domain.stepsRepository.StepsDataSource
import timber.log.Timber
import javax.inject.Inject


class StepsDataSourceImpl @Inject constructor(private val context: Context) : StepsDataSource {

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    private val sensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun getSteps(): Flow<Int> = callbackFlow {
        Timber.tag("StepsDataSource").d("Initializing step counter sensor...")

        // Проверка на наличие сенсора шагов в устройстве
        val packageManager = context.packageManager
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            Timber.tag("StepsDataSource").e("Device does not support step counter sensor")
            trySend(0)
            close()
            return@callbackFlow
        }

        if (sensor == null) {
            Timber.tag("StepsDataSource").e("Step counter sensor is not available")
            trySend(0) // Отправляем 0, чтобы UI не крашился
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val steps = event.values[0].toInt()
                    Timber.tag("StepsDataSource").d("Sensor changed: $steps steps")
                    trySend(steps).onFailure {
                        Timber.tag("StepsDataSource").e("Failed to send steps: ${it?.message}")
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Timber.tag("StepsDataSource").d("Sensor accuracy changed: $accuracy")
            }
        }

        val registered = try {
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        } catch (e: Exception) {
            Timber.tag("StepsDataSource").e("Exception while registering sensor listener: ${e.message}")
            false
        }

        if (!registered) {
            Timber.tag("StepsDataSource").e("Failed to register sensor listener")
            trySend(0) // Если не удалось зарегистрировать, отправляем 0 шагов
            close()
        } else {
            Timber.tag("StepsDataSource").d("Sensor listener registered successfully")
        }

        awaitClose {
            Timber.tag("StepsDataSource").d("Unregistering sensor listener")
            sensorManager.unregisterListener(listener)
        }
    }.catch { e ->
        Timber.tag("StepsDataSource").e("Error in getSteps(): ${e.message}")
        emit(0) // Если произошла ошибка, отправляем 0 шагов
    }.flowOn(Dispatchers.IO)


}
