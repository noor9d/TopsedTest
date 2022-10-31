package com.example.topsedtest.ui.fragments.dashboard

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.topsedtest.R
import com.example.topsedtest.data.model.DashboardData
import com.example.topsedtest.databinding.FragmentDashboardBinding
import com.example.topsedtest.domain.drive.currentDrive.CurrentDriveStatus.*
import com.example.topsedtest.ui.home.HomeViewModel
import com.example.topsedtest.ui.permission.PermissionCheckActivity
import com.example.topsedtest.utils.DialogUtils
import com.example.topsedtest.utils.LocationPermissionUtils
import com.example.topsedtest.utils.observeForChange
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DashboardFragment : Fragment() {
    private var isShowingTopSpeedAnimation = false
    private var lastStatus = -1
    private lateinit var viewBinding: FragmentDashboardBinding

    private val homeViewModel: HomeViewModel by sharedViewModel()

    private val permissionActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (LocationPermissionUtils.isLocationEnabled(requireContext())
                && LocationPermissionUtils.isBasicPermissionGranted(requireContext())
            ) {
                startDrive()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentDashboardBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lastStatus = -1
        homeViewModel.dashboardLiveData.observe(viewLifecycleOwner) {
            setDashboardStatus(it)
        }

        homeViewModel.newTopSpeedLiveData.observeForChange(viewLifecycleOwner, {
            showTopSpeedAnimation(it)
        }, false)

        val defaultTextView = TextView(requireContext()).apply {
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Display3)
        }
        viewBinding.arcProgress.textColor = defaultTextView.currentTextColor

        viewBinding.startBtn.setOnClickListener {
            if (LocationPermissionUtils.isBasicPermissionGranted(requireContext())
                && LocationPermissionUtils.isLocationEnabled(requireContext())
            ) {
                startDrive()
            } else {
                permissionActivityResultLauncher.launch(
                    PermissionCheckActivity.getOpenIntent(
                        context = requireContext(),
                        isPromptMode = false
                    )
                )
            }
        }

        viewBinding.pauseBtn.setOnClickListener {
            if (homeViewModel.dashboardLiveData.value?.isPaused() == true) {
                homeViewModel.startDrive()
            } else {
                homeViewModel.pauseDrive()
            }
        }

        viewBinding.stopBtn.setOnClickListener {
            DialogUtils.createDialog(
                context = requireContext(),
                message = getString(R.string.stop_ride),
                positiveAction = getString(R.string.stop),
                negativeAction = getString(R.string.no),
                onSuccessAction = {
                    stopDrive()
                }).show()
        }
    }

    private fun stopDrive() {
        homeViewModel.stopDrive()
    }

    private fun startDrive() {
        homeViewModel.startDrive()
    }

    private fun setDashboardStatus(dashboardData: DashboardData) {
        if (lastStatus != dashboardData.getStatus()) {
            lastStatus = dashboardData.getStatus()
            when (dashboardData.getStatus()) {
                STARTING -> {
                    viewBinding.pauseBtn.supportBackgroundTintList = ColorStateList.valueOf(
                        ActivityCompat.getColor(
                            requireContext(),
                            R.color.pauseOrange
                        )
                    )
                    viewBinding.pauseBtn.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause
                        )
                    )
                    viewBinding.loadingLayout.visibility = View.VISIBLE
                    viewBinding.loadingText.text = getString(
                        R.string.waiting_for_gps_signal_d,
                        dashboardData.getGPSSignalStrength()
                    )
                    viewBinding.startBtn.hide()
                    viewBinding.stopBtn.show()
                    viewBinding.pauseBtn.show()
                    viewBinding.parentContainer.keepScreenOn = true
                }
                STARTED -> {
                    viewBinding.pauseBtn.supportBackgroundTintList = ColorStateList.valueOf(
                        ActivityCompat.getColor(
                            requireContext(),
                            R.color.pauseOrange
                        )
                    )
                    viewBinding.pauseBtn.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause
                        )
                    )
                    viewBinding.loadingLayout.visibility = View.GONE
                    viewBinding.startBtn.hide()
                    viewBinding.stopBtn.show()
                    viewBinding.pauseBtn.show()
                    viewBinding.parentContainer.keepScreenOn = true
                }
                PAUSED -> {
                    viewBinding.pauseBtn.supportBackgroundTintList = ColorStateList.valueOf(
                        ActivityCompat.getColor(
                            requireContext(),
                            R.color.purple_500
                        )
                    )
                    viewBinding.pauseBtn.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_play
                        )
                    )
                    viewBinding.loadingLayout.visibility = View.GONE
                    viewBinding.startBtn.hide()
                    viewBinding.stopBtn.show()
                    viewBinding.pauseBtn.show()
                    viewBinding.parentContainer.keepScreenOn = false
                }
                STOPPED -> {
                    viewBinding.pauseBtn.supportBackgroundTintList = ColorStateList.valueOf(
                        ActivityCompat.getColor(
                            requireContext(),
                            R.color.pauseOrange
                        )
                    )
                    viewBinding.pauseBtn.setImageDrawable(
                        ActivityCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pause
                        )
                    )
                    viewBinding.loadingLayout.visibility = View.GONE
                    viewBinding.startBtn.show()
                    viewBinding.stopBtn.hide()
                    viewBinding.pauseBtn.hide()
                    viewBinding.parentContainer.keepScreenOn = false
                }
            }
        }

        viewBinding.arcProgress.setData(
            if (dashboardData.getTopSpeed() > 140.0) dashboardData.getTopSpeed() else 140,
            dashboardData.getCurrentSpeed(),
            dashboardData.timeText()
        )
        viewBinding.tvAvgSpeedValue.text = dashboardData.getAverageSpeedText()
//        viewBinding.topSpeedText.text = dashboardData.getTopSpeedText()

        viewBinding.tvDistanceValue.text = dashboardData.getDistanceText()

        val speedUnitText = dashboardData.getSpeedUnitText()
        val distanceUnitText = dashboardData.getDistanceUnitText()

        viewBinding.tvDistanceUnit.text = distanceUnitText
        viewBinding.tvAvgSpeedUnit.text = speedUnitText
//        viewBinding.topSpeedUnitText.text = speedUnitText
        viewBinding.arcProgress.metricText = speedUnitText
    }

    private fun showTopSpeedAnimation(topSpeedString: String) {
        try {
            if (!isShowingTopSpeedAnimation) {
                isShowingTopSpeedAnimation = true
                /*viewBinding.topSpeedText.text = topSpeedString
                viewBinding.topSpeedText.setTextColor(
                    ActivityCompat.getColor(
                        requireContext(),
                        R.color.stopRed
                    )
                )*/
                val anim: Animation = AlphaAnimation(0.0f, 1.0f)
                anim.duration = 50 //You can manage the blinking time with this parameter
                anim.startOffset = 20
                anim.repeatMode = Animation.REVERSE
                anim.repeatCount = 20 //50*50 = 1500 2.5secs
//                viewBinding.topSpeedText.startAnimation(anim)
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        if (isAdded) {
                            isShowingTopSpeedAnimation = false
                            val fontName = "fonts/digital.ttf"
                            val typeface =
                                Typeface.createFromAsset(requireContext().assets, fontName)
                            /*viewBinding.topSpeedText.setTextAppearance(
                                androidx.appcompat.R.style.TextAppearance_AppCompat_Display2
                            )
                            viewBinding.topSpeedText.typeface = typeface*/
                        }
                    } catch (ignore: Exception) {

                    }
                }, 1500)
            }
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
    }

    companion object {
        const val TAG = "DashboardFragment1"
    }
}