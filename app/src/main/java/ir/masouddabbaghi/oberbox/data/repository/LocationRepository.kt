package ir.masouddabbaghi.oberbox.data.repository

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        fun getLastLocation(callback: (Result<LatLng>) -> Unit) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    callback(Result.success(LatLng(it.latitude, it.longitude)))
                } ?: callback(Result.failure(Exception("Location not found")))
            }
        }
    }
