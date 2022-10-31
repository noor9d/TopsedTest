package com.example.topsedtest.domain.drive

import com.bike.race.domain.drive.Drive
import com.example.topsedtest.domain.location.LocalityInfoCollector
import com.example.topsedtest.repositories.DriveRepository

class DriveLocalityAddService(
    private val driveRepository: DriveRepository,
    private val localityInfoCollector: LocalityInfoCollector
) {

    suspend fun addLocalityInformation() {

        val drives = driveRepository.getDrivesWithNoLocalityInfo()

        drives.forEach {
            getAndUpdateLocality(it)
        }
    }

    suspend fun getAndUpdateLocality(drive: Drive): Drive? {
        val startLocality = localityInfoCollector.getLocalityInfo(
            drive.startLocation.latitude,
            drive.startLocation.longitude
        )
        val endLocality = localityInfoCollector.getLocalityInfo(
            drive.endLocation.latitude,
            drive.endLocation.longitude
        )

        return if (startLocality != null && endLocality != null) {
            driveRepository.updateDriveLocality(drive.getId(), startLocality, endLocality)
            drive.copy(startLocality = startLocality, endLocality = endLocality)
        } else {
            null
        }
    }
}