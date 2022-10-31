package com.example.topsedtest.ui.home

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import android.os.PersistableBundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.topsedtest.service.DriveBackGroundService
import com.example.topsedtest.R
import com.example.topsedtest.data.model.DashboardData
import com.example.topsedtest.databinding.ActivityMainBinding
import com.example.topsedtest.service.ActivityMessenger
import com.example.topsedtest.utils.DriveAction.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), ServiceConnection {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: HomeViewModel by viewModel()
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.fragNavHost) as NavHostFragment).navController
    }

    private val activityMessenger =
        ActivityMessenger(
            ::onStatusUpdate,
            ::onDriveFinished
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        startService(Intent(this, DriveBackGroundService::class.java))

        binding.bottomNavigation.setupWithNavController(navController)

        viewModel.startStopLiveData.observe(this) {
            when (it) {
                START -> {
                    activityMessenger.startDrive()
                    binding.root.keepScreenOn = true
                }
                PAUSE -> {
                    activityMessenger.pauseDrive()
                    binding.root.keepScreenOn = false
                }
                STOP -> {
                    activityMessenger.stopDrive()
                    binding.root.keepScreenOn = false
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        viewModel.syncServices()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        activityMessenger.onDisconnect()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        activityMessenger.onConnect(Messenger(service))
        activityMessenger.handShake()
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, DriveBackGroundService::class.java),
            this,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        unbindService(this)
    }

    private fun onStatusUpdate(dashboardData: DashboardData) {
        viewModel.updateDashboardData(dashboardData)
    }

    private fun onDriveFinished(raceId: Long) {
//        openDriveReport(raceId)
        Log.d(TAG, "onDriveFinished: race id= $raceId")
    }

    companion object {
        const val TAG = "MainActivity1"
    }
}