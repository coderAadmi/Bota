package com.poloman.bota

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import com.poloman.bota.network.NetworkService
import com.poloman.bota.network.TransferProgress
import com.poloman.bota.service.MonitorService
import com.poloman.bota.ui.theme.BotaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class MouseActivity : ComponentActivity(), SensorEventListener {

     var sensor: Sensor? = null
    lateinit var sensorManager : SensorManager

    var networkService: NetworkService? = null

    var isClientConnected = false

    private val netConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onServiceConnected(
            name: ComponentName?,
            service: IBinder?
        ) {
            networkService = (service as NetworkService.NetworkServiceBinder).getService()
            Log.d("BTU_BND", "Net Service bound")

            networkService?.let { service ->
                service.networkCallbackFromActivity = object : NetworkService.NetworkCallback{
                    override fun onConnectionRequest(from: String, ip: String) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataIncomingRequest(
                        from: String,
                        ip: String,
                        fileName: String,
                        size: Long
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onMultipleFilesIncomingRequest(
                        from: String,
                        ip: String,
                        fileCount: Int,
                        size: Long
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onServerStarted() {
                        TODO("Not yet implemented")
                    }

                    override fun onIncomingProgressChange(
                        ip: String,
                        progress: TransferProgress
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onOutgoingProgressChange(
                        ip: String,
                        progress: TransferProgress
                    ) {
                        TODO("Not yet implemented")
                    }

                    override fun onStatusChange(
                        ip: String,
                        progress: TransferProgress
                    ) {
                        TODO("Not yet implemented")
                    }


                }
            }

            networkService?.initServer()


        }

        override fun onServiceDisconnected(name: ComponentName?) {
            networkService = null
            Log.d("BTU_BND", "Network Service Unbound")
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BotaTheme {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (center) = createRefs()
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "",
                        modifier = Modifier.constrainAs(center) {
                            centerHorizontallyTo(parent)
                            centerVerticallyTo(parent)

                        }
                    )
                }
            }
        }
        startNetService()
         sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
//            Log.d("BOTA_SEN","X = ${it.values[0]}  Y = ${it.values[1]}  Z = ${it.values[2]}")
            updateCursorFromAccel(it.values[0],it.values[1])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onResume() {
        super.onResume()
        sensor?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private val SENSITIVITY = 5f         // Adjust for movement speed
    private val MIN_DELTA = 0.2f         // Ignore tiny noise
    private val MAX_ACCEL = 5f           // Clamp to avoid spikes

    // State variables

    private var filteredAx = 0f
    private var filteredAy = 0f


    // Called on each sensor update
    fun onAccelerometerUpdate(ax: Float, ay: Float, az: Float) {
        // Filter the input
        filteredAx = lowPassFilter(ax.coerceIn(-MAX_ACCEL, MAX_ACCEL), filteredAx)
        filteredAy = lowPassFilter(ay.coerceIn(-MAX_ACCEL, MAX_ACCEL), filteredAy)

        // Apply dead zone filtering
        val deltaX = if (abs(filteredAx) > MIN_DELTA) filteredAx * SENSITIVITY else 0f
        val deltaY = if (abs(filteredAy) > MIN_DELTA) filteredAy * SENSITIVITY else 0f

        // Update cursor position
        cursorX += deltaX
        cursorY += deltaY

        // Clamp to screen bounds (replace with actual screen dimensions)
        cursorX = cursorX.coerceIn(0f, 1920f)
        cursorY = cursorY.coerceIn(0f, 1080f)

//        updateCursor(cursorX, cursorY)
    }



    private val MOVEMENT_THRESHOLD = 0.5f


    var lastSentX = -1
    var lastSentY = -1

    var lastSentTime = 0L
    // Last known screen position of the cursor


    private val alpha = 0.8f
    private var lastX = 0f
    private var lastY = 0f
    private var cursorX = 960f
    private var cursorY = 540f
    private val sensitivity = 12f

    fun updateCursorFromAccel(accelX: Float, accelY: Float) {
        // Low-pass filter
        val filteredX = alpha * lastX + (1 - alpha) * accelX
        val filteredY = alpha * lastY + (1 - alpha) * accelY

        lastX = filteredX
        lastY = filteredY

        cursorX += -filteredX * sensitivity
        cursorY += filteredY * sensitivity

        cursorX = cursorX.coerceIn(0f, 1920f)
        cursorY = cursorY.coerceIn(0f, 1080f)


        Log.d("CURSOR_MOVE", "Cursor moved to X: $cursorX, Y: $cursorY")
        CoroutineScope(Dispatchers.IO).launch {
            networkService?.let {
                if(isClientConnected)
                    it.sendMousePos(cursorX.toInt(), cursorY.toInt())
            }
        }
    }

    fun lowPassFilter(input: Float, output: Float, alpha: Float = 0.8f): Float {
        return alpha * output + (1 - alpha) * input
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if(netConnection!= null){
                unbindService(netConnection)
            }
        }
        catch (e : Exception){

        }
    }

    fun startNetService() {
        Intent(applicationContext, NetworkService::class.java).also {
            it.action = MonitorService.Action.START_MONITOR.toString()
            startForegroundService(it)
        }
        bindService(Intent(this, NetworkService::class.java), netConnection, BIND_ABOVE_CLIENT)
    }
}