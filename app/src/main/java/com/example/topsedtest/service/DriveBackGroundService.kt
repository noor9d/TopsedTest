package com.example.topsedtest.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import com.bike.race.components.service.MessengerProtocol
import com.example.topsedtest.data.model.DashboardData
import com.example.topsedtest.domain.drive.DriveService
import com.example.topsedtest.utils.NotificationUtils
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

/**
 * Background Service(ForeGround Service in Android Context) to listen to GPS Signal in the background and hold the Drive Status.
 */
class DriveBackGroundService : Service() {

    private val driveService: DriveService by inject()
    private var isForeGround: Boolean = false
    private val serviceMessenger =
        ServiceMessenger(::onCommandReceived)

    /**
     * This ensures locking Android to not go to sleep and it will help listening to GPS Signal even if the phone went to idle state.
     */
    private val wakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PARTIAL_WAKE_LOCK,
            packageName
        ).also {
            it.setReferenceCounted(false)
        }
    }

    override fun onCreate() {
        super.onCreate()

        driveService.registerCallback(
            dashboardDataCallback = { dashboardData ->
                serviceMessenger.sendDashboardData(dashboardData)
                checkAndUpdateCPUWake(dashboardData)
            },
            driveFinishCallback = { driveId ->
                serviceMessenger.sendRaceFinished(driveId)
                releaseWakelock()
            }
        )
    }

    private fun startForeGround() {
        isForeGround = true
        startForeground(
            NotificationUtils.TOPSED_RACE_NOTIFICATION_ID,
            NotificationUtils.getRacingNotification(this)
        )
    }

    private fun stopForeground() {
        isForeGround = false
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        stopForeground(true)
        return serviceMessenger.getBinder()
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        stopForeground()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (driveService.isRaceOngoing()) {
            startForeGround()
        } else {
            stopForeground()
        }
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun onCommandReceived(@MessengerProtocol.Command command: Int) {
        when (command) {
            MessengerProtocol.COMMAND_HANDSHAKE -> {
                serviceMessenger.sendDashboardData(driveService.getDashboardData())
            }
            MessengerProtocol.COMMAND_START -> {
                driveService.startDrive()
            }
            MessengerProtocol.COMMAND_PAUSE -> {
                driveService.pauseDrive()
            }
            MessengerProtocol.COMMAND_STOP -> {
                stopRace()
            }
        }
    }

    private fun stopRace() {
        driveService.stopDrive()
        stopForeground()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun checkAndUpdateCPUWake(dashboardData: DashboardData) {
        if (dashboardData.isRunning().not()) {
            releaseWakelock()
        } else if (wakeLock.isHeld.not()) {
            wakeLock.acquire(TimeUnit.HOURS.toMillis(2))
        }
    }

    private fun releaseWakelock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

}