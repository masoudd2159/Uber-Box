package ir.masouddabbaghi.oberbox.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import ir.masouddabbaghi.oberbox.R
import ir.masouddabbaghi.oberbox.databinding.ActivityMapsBinding
import ir.masouddabbaghi.oberbox.ui.base.BaseActivity
import ir.masouddabbaghi.oberbox.ui.viewmodel.MapsViewModel

@AndroidEntryPoint
class MapsActivity :
    BaseActivity(),
    OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel: MapsViewModel by viewModels()
    private val locationPermissionRequestCode = 1

    override fun getLayoutResourceBinding(): View {
        binding = ActivityMapsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionRequestCode)
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.isMyLocationEnabled = true

                mapsViewModel.location.observe(
                    this,
                    Observer { latLng ->
                        mMap.addMarker(MarkerOptions().position(latLng).title("You are here"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    },
                )
                mapsViewModel.fetchLocation()
            } catch (e: SecurityException) {
                Toast.makeText(this, "Location permission is required to show your current location on the map.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequestCode) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                // Permission was denied.
                Toast.makeText(this, "Location permission is required to show your current location on the map.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
