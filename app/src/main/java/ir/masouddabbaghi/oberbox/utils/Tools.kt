package ir.masouddabbaghi.oberbox.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil

object Tools {
    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showCurvedPolyline(
        p1: LatLng,
        p2: LatLng,
        k: Double,
        mMap: GoogleMap,
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
        val pattern = listOf(Dash(30f), Gap(20f))

        // Calculate heading between circle center and two points
        val h1: Double = SphericalUtil.computeHeading(c, p1)
        val h2: Double = SphericalUtil.computeHeading(c, p2)

        // Calculate positions of points on circle border and add them to polyline options
        val numPoints = 100
        val step = (h2 - h1) / numPoints

        for (i in 0 until numPoints) {
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
}
