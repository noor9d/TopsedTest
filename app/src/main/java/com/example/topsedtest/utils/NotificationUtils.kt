package com.example.topsedtest.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.topsedtest.R
import com.example.topsedtest.ui.home.MainActivity

object NotificationUtils {

    private const val FOREGROUND_CHANNEL_ID = "Speedometer"
    private const val REQ_CODE_OPEN_ACTIVITY = 1
    const val TOPSED_RACE_NOTIFICATION_ID = 1

    private fun checkAndCreateChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.getSystemService(NotificationManager::class.java)?.let { notificationManager ->

                if (notificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {

                    val serviceChannel = NotificationChannel(
                        FOREGROUND_CHANNEL_ID,
                        FOREGROUND_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_HIGH
                    )

                    notificationManager.createNotificationChannel(serviceChannel)
                }
            }
        }

    }

    fun getRacingNotification(
        context: Context,
    ): Notification {

        checkAndCreateChannel(context)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQ_CODE_OPEN_ACTIVITY,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Speedometer Running")
            .setContentText("Keep the speedometer app open for accurate speed calculation")
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_speedometer)
            .setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }
}