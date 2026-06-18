package com.example.cityflowbkk.features.route

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.cityflowbkk.MainActivity
import com.example.cityflowbkk.R

class TransitArrivalNotifier(
    private val context: Context,
) {
    fun showArrivalAlert(stationName: String) {
        createNotificationChannel()
        vibrate()

        if (!canPostNotifications()) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_ROUTE
            putExtra(MainActivity.EXTRA_OPEN_ROUTE, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ROUTE_PENDING_INTENT_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Next Station")
            .setContentText("Your next station is $stationName.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Your next station is $stationName.\n" +
                            "This is your drop-off station. Please prepare to leave the train.",
                    ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ARRIVAL_NOTIFICATION_ID, notification)
    }

    fun showDestinationArrival() {
        createNotificationChannel()
        vibrate()

        if (!canPostNotifications()) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_ROUTE
            putExtra(MainActivity.EXTRA_OPEN_ROUTE, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ROUTE_PENDING_INTENT_REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Arrived")
            .setContentText("Arrived at Destination")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You have arrived at your destination."),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(ARRIVAL_NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Transit arrival alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts before reaching BTS, MRT, and Airport Rail Link destination stations."
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            vibrationPattern = longArrayOf(0L, VIBRATION_DURATION_MS)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.createOneShot(
                VIBRATION_DURATION_MS,
                VibrationEffect.DEFAULT_AMPLITUDE,
            )
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(
                VIBRATION_DURATION_MS,
                VibrationEffect.DEFAULT_AMPLITUDE,
            )
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VIBRATION_DURATION_MS)
        }
    }

    companion object {
        private const val CHANNEL_ID = "transit_arrival_alerts"
        private const val ARRIVAL_NOTIFICATION_ID = 4102
        private const val ROUTE_PENDING_INTENT_REQUEST_CODE = 4202
        private const val VIBRATION_DURATION_MS = 2_000L
    }
}
