package ir.masouddabbaghi.oberbox.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.masouddabbaghi.oberbox.data.repository.LocationRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsViewModel
    @Inject
    constructor(
        private val locationRepository: LocationRepository,
    ) : ViewModel() {
        private val _location = MutableLiveData<LatLng>()
        val location: LiveData<LatLng> get() = _location

        fun fetchLocation() {
            viewModelScope.launch {
                locationRepository.getLastLocation { result ->
                    result
                        .onSuccess { latLng ->
                            _location.postValue(latLng)
                        }.onFailure {
                            // Handle failure
                        }
                }
            }
        }
    }
