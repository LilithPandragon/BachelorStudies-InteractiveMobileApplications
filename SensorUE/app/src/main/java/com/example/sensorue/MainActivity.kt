package com.example.sensorue

import MainViewModel
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    private lateinit var temperatureTextView: TextView
    private lateinit var pressureTextView: TextView

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        temperatureTextView = findViewById(R.id.temperatureTextView)
        pressureTextView = findViewById(R.id.pressureTextView)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.initialize(getSystemService(SENSOR_SERVICE) as SensorManager)

        viewModel.temperatureLiveData.observe(this, Observer { temperature ->
            temperatureTextView.text = "Temperature: $temperature °C"
        })

        viewModel.pressureLiveData.observe(this, Observer { pressure ->
            pressureTextView.text = "Pressure: $pressure hPa"
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.startSensors()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopSensors()
    }
}
