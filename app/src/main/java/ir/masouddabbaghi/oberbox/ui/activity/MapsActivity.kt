package ir.masouddabbaghi.oberbox.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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
    private lateinit var customMarkerView: View
    private val mapsViewModel: MapsViewModel by viewModels()
    private val locationPermissionRequestCode = 1

    override fun getLayoutResourceBinding(): View {
        binding = ActivityMapsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customMarkerView = LayoutInflater.from(this@MapsActivity).inflate(R.layout.custom_marker, null)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.apply {
            buttonLocate.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this@MapsActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@MapsActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        locationPermissionRequestCode,
                    )
                } else {
                    fetchUserLocation()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MapsActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode,
            )
        } else {
            enableMyLocation()
        }

        mMap.uiSettings.isMyLocationButtonEnabled = false

        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addContentView(customMarkerView, layoutParams)

        // Update marker position on camera move
        mMap.setOnCameraMoveListener {
            val centerLatLng = mMap.cameraPosition.target
            Log.i(tagLog, "Center latitude: ${centerLatLng.latitude}, longitude: ${centerLatLng.longitude}")
            // Update any UI or marker position if needed
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            try {
                mMap.isMyLocationEnabled = true
                mapsViewModel.location.observe(this) { latLng -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f)) }
                mapsViewModel.fetchLocation()
            } catch (e: SecurityException) {
                Toast
                    .makeText(
                        this@MapsActivity,
                        "Location permission is required to show your current location on the map.",
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }
    }

    private fun fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            mapsViewModel.location.observe(this) { latLng -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f)) }
            mapsViewModel.fetchLocation()
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
                Toast
                    .makeText(
                        this@MapsActivity,
                        "Location permission is required to show your current location on the map.",
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }
    }
}
