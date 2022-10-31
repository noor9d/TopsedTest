package com.example.topsedtest.di

import android.content.Context
import android.location.LocationManager
import com.example.topsedtest.components.LocationProvider
import com.example.topsedtest.components.SingleLocationProvider
import com.example.topsedtest.db.AppDatabase
import com.example.topsedtest.domain.drive.DriveLocalityAddService
import com.example.topsedtest.domain.drive.DriveService
import com.example.topsedtest.domain.drive.DriveStatAnalyser
import com.example.topsedtest.domain.drive.MapPolyLineCreator
import com.example.topsedtest.domain.drive.currentDrive.EndForgotCalculator
import com.example.topsedtest.domain.drive.currentDrive.PauseCalculator
import com.example.topsedtest.domain.drive.drivepath.DrivePathBuilder
import com.example.topsedtest.domain.drive.drivepath.DrivePathFilter
import com.example.topsedtest.domain.drive.drivepath.PathAngleDiffChecker
import com.example.topsedtest.domain.drive.drivepath.SpeedDiffCalculator
import com.example.topsedtest.domain.location.LocalityInfoCollector
import com.example.topsedtest.preference.UserPreferenceManager
import com.example.topsedtest.repositories.DriveRepository
import com.example.topsedtest.ui.home.HomeViewModel
import com.example.topsedtest.utils.ClockUtils
import com.example.topsedtest.utils.ConversionUtil
import com.example.topsedtest.utils.SphericalUtil
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val koinModule = module {

    single { DriveService() }

    single { DriveLocalityAddService(get(), get()) }

//    factory { PrivacyPolicyService(get()) }

//    factory { StateViewProvider() }

    factory { LocationProvider() }

    factory { androidContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    factory { ClockUtils() }

    factory { ConversionUtil() }

    factory { SphericalUtil() }

    factory { EndForgotCalculator() }

    factory { DriveRepository(get()) }

    factory { MapPolyLineCreator() }

    factory { DrivePathBuilder() }

    factory { PauseCalculator() }

    factory { PathAngleDiffChecker() }

    factory { SpeedDiffCalculator() }

    factory { DriveStatAnalyser() }

    factory { LocalityInfoCollector(androidContext()) }

    factory { UserPreferenceManager(androidContext()) }

    single { AppDatabase.invoke(androidContext()) }

    factory { SingleLocationProvider(get()) }

    factory { DrivePathFilter() }

    viewModel { HomeViewModel(get()) }

//    viewModel { SplashViewModel(get()) }

//    viewModel { DashboardViewModel() }

//    viewModel { MyDrivesViewModel(get(), get()) }

//    viewModel { DriveReportViewModel(get(), get(), get(), get(), get(), get()) }

//    viewModel { MyAllDrivesViewModel(get()) }

//    viewModel { CompareViewModel(get(), get()) }
}