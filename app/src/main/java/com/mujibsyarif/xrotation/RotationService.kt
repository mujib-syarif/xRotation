package com.mujibsyarif.xrotation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.View
import android.view.WindowManager

/**
 * Layanan latar depan yang memasang jendela overlay 1x1 piksel tak terlihat.
 * Orientasi jendela itu memaksa orientasi layar, termasuk untuk app/game lain.
 */
class RotationService : Service() {

    private var overlay: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Prefs.setEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForeground()

        if (!Prefs.isEnabled(this) || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        applyMode(Prefs.mode(this))
        return START_STICKY
    }

    private fun applyMode(mode: Mode) {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            1,
            1,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )
        params.screenOrientation = mode.orientation

        val existing = overlay
        if (existing == null) {
            val v = View(this)
            wm.addView(v, params)
            overlay = v
        } else {
            wm.updateViewLayout(existing, params)
        }
    }

    private fun removeOverlay() {
        val v = overlay ?: return
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        try {
            wm.removeView(v)
        } catch (_: Exception) {
        }
        overlay = null
    }

    private fun startAsForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Layanan xRotation", NotificationManager.IMPORTANCE_LOW),
        )

        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, RotationService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_rotation)
            .setContentTitle("xRotation aktif")
            .setContentText("Orientasi dikunci: " + Prefs.mode(this).fullName)
            .setContentIntent(openApp)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_stat_rotation),
                    "Matikan",
                    stop,
                ).build(),
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.mujibsyarif.xrotation.STOP"
        private const val CHANNEL_ID = "xr_service"
        private const val NOTIF_ID = 1

        fun start(context: Context) {
            context.startForegroundService(Intent(context, RotationService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RotationService::class.java))
        }
    }
}
