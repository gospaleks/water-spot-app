package rs.gospaleks.waterspot.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import rs.gospaleks.waterspot.common.NotificationHelper

class LocationTrackingService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val INTERVAL_MS = 60_000L // pravi minut!
        private const val TAG = "LocationTrackingService"
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Inicijalna notifikacija koja se prikazuje kada servis pocne da radi
        val notification = notificationHelper.createPersistentNotification(
            title = "Location tracking",
            message = "Location tracking service is running. You will receive notification when a spot is detected.",
        )

        startForeground(NOTIFICATION_ID, notification)

        runnable = object : Runnable {
            override fun run() {
                Log.d(TAG, "Servis aktivan - logujem svakih 1 minut")
                notificationHelper.showNotification(2, "Spot blizu!", "Imate spot u krugu od 50m")

                handler.postDelayed(this, INTERVAL_MS)
            }
        }
        handler.post(runnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        Log.d(TAG, "Servis zaustavljen")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
