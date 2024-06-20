package ir.masouddabbaghi.oberbox.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.AndroidEntryPoint
import ir.masouddabbaghi.oberbox.R
import ir.masouddabbaghi.oberbox.databinding.ActivityMapsBinding
import ir.masouddabbaghi.oberbox.ui.base.BaseActivity
import ir.masouddabbaghi.oberbox.ui.viewmodel.MapsViewModel
import java.util.Arrays

@AndroidEntryPoint
class MapsActivity :
    BaseActivity(),
    OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var customMarkerView: View
    private val mapsViewModel: MapsViewModel by viewModels()
    private val locationPermissionRequestCode = 1
    private lateinit var point1: LatLng
    private lateinit var point2: LatLng

    override fun getLayoutResourceBinding(): View {
        binding = ActivityMapsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customMarkerView = LayoutInflater.from(this@MapsActivity).inflate(R.layout.custom_marker_primary_color, null)

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

            buttonChooseOrigin.setOnClickListener {
                val centerLatLng = mMap.cameraPosition.target
                point1 = centerLatLng
                val markerView = LayoutInflater.from(this@MapsActivity).inflate(R.layout.custom_marker_origin, null)
                addCustomMarker(centerLatLng, markerView)

                buttonChooseOrigin.visibility = View.GONE
                buttonBack.visibility = View.VISIBLE
                buttonChooseDestination.visibility = View.VISIBLE

                customMarkerView.visibility = View.GONE
                customMarkerView = LayoutInflater.from(this@MapsActivity).inflate(R.layout.custom_marker_secondary_color, null)
                val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                addContentView(customMarkerView, layoutParams)
            }

            buttonChooseDestination.setOnClickListener {
                val centerLatLng = mMap.cameraPosition.target
                point2 = centerLatLng
                val markerView = LayoutInflater.from(this@MapsActivity).inflate(R.layout.custom_marker_destination, null)
                addCustomMarker(centerLatLng, markerView)

                buttonBack.visibility = View.VISIBLE
                buttonChooseDestination.visibility = View.INVISIBLE

                customMarkerView.visibility = View.GONE

                showCurvedPolyline(point1, point2, 0.40)
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
        mMap.isMyLocationEnabled = false

        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addContentView(customMarkerView, layoutParams)

        // Update marker position on camera move
        mMap.setOnCameraMoveListener {
            val centerLatLng = mMap.cameraPosition.target
            Log.i(tagLog, "Center latitude: ${centerLatLng.latitude}, longitude: ${centerLatLng.longitude}")
            // Update any UI or marker position if needed
        }
    }

    private fun showCurvedPolyline(
        p1: LatLng,
        p2: LatLng,
        k: Double,
    ) {
        // Calculate distance and heading between two points
        val d: Double = SphericalUtil.computeDistanceBetween(p1, p2)
        val h: Double = SphericalUtil.computeHeading(p1, p2)

        // Midpoint position
        val p: LatLng = SphericalUtil.computeOffset(p1, d * 0.5, h)

        // Apply some mathematics to calculate position of the circle center
        val x = (1 - k * k) * d * 0.5 / (2 * k)
        val r = (1 + k * k) * d * 0.5 / (2 * k)

        val c: LatLng = SphericalUtil.computeOffset(p, x, h + 90.0)

        // Polyline options
        val options = PolylineOptions()
        val pattern = Arrays.asList(Dash(30f), Gap(20f))

        // Calculate heading between circle center and two points
        val h1: Double = SphericalUtil.computeHeading(c, p1)
        val h2: Double = SphericalUtil.computeHeading(c, p2)

        // Calculate positions of points on circle border and add them to polyline options
        val numpoints = 100
        val step = (h2 - h1) / numpoints

        for (i in 0 until numpoints) {
            val pi: LatLng = SphericalUtil.computeOffset(c, r, h1 + i * step)
            options.add(pi)
        }

        // Draw polyline
        mMap.addPolyline(
            options
                .width(10f)
                .color(Color.BLACK)
                .geodesic(false)
                .pattern(pattern),
        )
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

    private fun addCustomMarker(
        latLng: LatLng,
        markerView: View,
    ) {
        val markerBitmap = createBitmapFromView(markerView)
        mMap.addMarker(
            MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(markerBitmap)).title("Custom Marker"),
        )
    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
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
