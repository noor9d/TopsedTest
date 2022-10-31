package com.example.topsedtest.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.topsedtest.R
import com.example.topsedtest.data.model.DashboardData
import com.example.topsedtest.databinding.FragmentMapBinding
import com.example.topsedtest.domain.drive.currentDrive.CurrentDriveStatus
import com.example.topsedtest.ui.home.HomeViewModel
import com.example.topsedtest.utils.LocationTracker
import com.example.topsedtest.utils.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by sharedViewModel()
    private lateinit var mMap: GoogleMap
    private var latLng = LatLng(0.0, 0.0)
    private var lastStatus = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LocationTracker.getInstance(requireContext())!!.connectToLocation(object :
            LocationTracker.OnLocationUpdateListener {
            override fun onUpdate(lat: Double, lon: Double) {
                Log.d(TAG, "onUpdate: lat: $lat ---- lon: $lon")
                latLng = LatLng(lat, lon)
            }
        })
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        lastStatus = -1
        homeViewModel.dashboardLiveData.observe(viewLifecycleOwner) {
            setDashboardStatus(it)
        }
    }

    private fun setDashboardStatus(dashboardData: DashboardData) {
        if (lastStatus != dashboardData.getStatus()) {
            lastStatus = dashboardData.getStatus()
            when (dashboardData.getStatus()) {
                CurrentDriveStatus.STARTING -> {
                    binding.loadingLayout.visibility = View.VISIBLE
                    binding.loadingText.text = getString(
                        R.string.waiting_for_gps_signal_d,
                        dashboardData.getGPSSignalStrength()
                    )
                    binding.parentContainer.keepScreenOn = true
                }
                CurrentDriveStatus.STARTED -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.parentContainer.keepScreenOn = true
                }
                CurrentDriveStatus.PAUSED -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.parentContainer.keepScreenOn = false
                }
                CurrentDriveStatus.STOPPED -> {
                    binding.loadingLayout.visibility = View.GONE
                    binding.parentContainer.keepScreenOn = false
                }
            }
        }

        val speedUnitText = dashboardData.getSpeedUnitText()
        val distanceUnitText = dashboardData.getDistanceUnitText()

        binding.tvTimerValue.text = dashboardData.timeText()
        binding.currentSpeed.text = "${dashboardData.getCurrentSpeed()}"
        binding.currentSpeedUnit.text = speedUnitText
        binding.tvAvgSpeedValue.text = dashboardData.getAverageSpeedText()
        binding.tvAvgSpeedUnit.text = speedUnitText
        binding.tvDistanceValue.text = dashboardData.getDistanceText()
        binding.tvDistanceUnit.text = distanceUnitText
    }

    private fun addMarker() {
        if (Utils.isGPSEnabled(requireContext())) {
            Log.d(TAG, "addMarker: gps enabled")

            // Add a marker and move the camera
            mMap.addMarker(MarkerOptions().position(latLng))!!.showInfoWindow()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            val loc = CameraUpdateFactory.newLatLngZoom(
                latLng, 18f
            )
            mMap.animateCamera(loc)
        } else {
            Log.d(TAG, "addMarker: gps disabled")
            Utils.enableLocationSettings(
                requireActivity(),
                REQUEST_CODE_CHECK_SETTINGS
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
       const val TAG = "MapFragment1"
        const val REQUEST_CODE_CHECK_SETTINGS = 79
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        addMarker()
    }
}