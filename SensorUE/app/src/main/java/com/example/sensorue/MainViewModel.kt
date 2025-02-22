import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var temperatureSensor: Sensor? = null
    private var pressureSensor: Sensor? = null

    private val _temperatureLiveData = MutableLiveData<Float>()
    val temperatureLiveData: LiveData<Float>
        get() = _temperatureLiveData

    private val _pressureLiveData = MutableLiveData<Float>()
    val pressureLiveData: LiveData<Float>
        get() = _pressureLiveData

    fun initialize(sensorManager: SensorManager) {
        this.sensorManager = sensorManager
        temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)


        if (temperatureSensor == null) {

        }
    }


    fun startSensors() {
        temperatureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        pressureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nicht benötigt, da TextViews nicht direkt aktualisiert werden
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                val temperature = event.values[0]
                _temperatureLiveData.value = temperature
            } else if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                val pressure = event.values[0]
                _pressureLiveData.value = pressure
            }
        }
    }
}
