package ir.masouddabbaghi.oberbox.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.masouddabbaghi.oberbox.data.repository.LocationRepository
import javax.inject.Inject

@HiltViewModel
class MapsViewModel
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
    ) : ViewModel() {
        val location: LiveData<LatLng> =
            locationRepository.location.map { location ->
                LatLng(location.latitude, location.longitude)
            }

        fun fetchLocation() {
            locationRepository.fetchLocation()
        }
    }
