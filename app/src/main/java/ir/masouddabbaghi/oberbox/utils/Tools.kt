package ir.masouddabbaghi.oberbox.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import carbon.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import ir.masouddabbaghi.oberbox.R
import java.io.ByteArrayOutputStream

object Tools {
    fun directLinkToBrowser(
        activity: Activity,
        url: String?,
    ) {
        Log.i(Tools::class.java.simpleName, "url : $url")
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            activity.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(activity, "Ops, Cannot open url", Toast.LENGTH_LONG).show()
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
            (
                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI,
                ) ||
                    capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR,
                    ) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            )
    }

    fun getPercent(
        total: Int,
        collected: Int,
    ): Float {
        if (total == 0) {
            return 0f
        }
        return (collected.toFloat() / total.toFloat()) * 100
    }

    fun cutString(string: String): String =
        if (string.length > 10) {
            string.substring(0, 10) + "..."
        } else {
            string
        }

    fun discount(
        price: String,
        percent: Int,
    ): String =
        if (percent > 0) {
            val priceDiscount: Long = (price.toLong() * percent) / 100
            priceDiscount.toString()
        } else {
            price
        }

    fun discount(
        price: Int,
        percent: Int,
    ): Long =
        if (percent > 0) {
            val priceDiscount: Long = ((price.toLong() * percent) / 100)
            price - priceDiscount
        } else {
            price
        }.toLong()

    fun isValidIranianNationalCode(input: String) =
        input.takeIf { it.length == 10 }?.mapNotNull(Char::digitToIntOrNull)?.takeIf { it.size == 10 }?.let {
            val check = it[9]
            val sum = it.slice(0..8).mapIndexed { i, x -> x * (10 - i) }.sum() % 11
            if (sum < 2) check == sum else check + sum == 11
        } ?: false

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun String.addCurrency(currency: String = "تومان"): String = "$this $currency"

    fun String.addPostfixCurrency(currency: String = "تومان"): String = "$currency $this"

    @Suppress("DEPRECATION")
    fun Context.vibrator(durationMillis: Long = 50) {
        Log.i(javaClass.simpleName, "VERSION SDK INT for Vibrator fun : ${Build.VERSION.SDK_INT}")
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                // For Android 12 (S) and above
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrationEffect = VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(vibrationEffect)
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                // For Android 8.0 (Oreo) to Android 11 (R)
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val vibrationEffect = VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            }

            else -> {
                // For Android versions below Oreo (API level 26)
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(durationMillis)
            }
        }
    }

    fun readJsonFromRawResource(
        resources: Resources,
        resourceId: Int,
    ): String =
        resources.openRawResource(resourceId).use { stream ->
            stream.bufferedReader().use {
                it.readText()
            }
        }

    fun formatWithDigitSeparator(
        number: Any,
        digits: Int = 3,
        separator: String = ",",
    ): String {
        val numberStr =
            when (number) {
                is Int, is Long, is Double -> number.toString()
                is String -> number
                else -> throw IllegalArgumentException("Unsupported number type")
            }

        val parts = numberStr.split(".")
        val integerPart = parts[0]
        val fractionalPart = if (parts.size > 1) ".${parts[1]}" else ""

        val isNegative = integerPart.startsWith('-')
        val unsignedIntegerPart = if (isNegative) integerPart.substring(1) else integerPart

        val length = unsignedIntegerPart.length

        val formatted = StringBuilder()
        for (i in 0 until length) {
            if (i > 0 && (length - i) % digits == 0) {
                formatted.append(separator)
            }
            formatted.append(unsignedIntegerPart[i])
        }

        return if (isNegative) "$formatted$fractionalPart-" else "$formatted$fractionalPart"
    }

    fun displayImageWithGlide(
        context: Context,
        imageView: ImageView,
        imageSource: Any?,
        isImageDrawable: Boolean = false,
    ) {
        val glideRequest =
            when {
                imageSource is String && isImageDrawable -> {
                    val drawableResId = context.resources.getIdentifier(imageSource, "drawable", context.packageName)
                    if (drawableResId != 0) {
                        Glide.with(context).load(drawableResId)
                    } else {
                        Glide.with(context).load(R.drawable.no_image)
                    }
                }

                imageSource is String && !isImageDrawable -> {
                    if (imageSource.isNotEmpty()) {
                        Glide.with(context).load(imageSource)
                    } else {
                        Glide.with(context).load(R.drawable.no_image)
                    }
                }

                imageSource is Int -> Glide.with(context).load(imageSource)
                imageSource is Bitmap -> Glide.with(context).load(imageSource)
                else -> Glide.with(context).load(R.drawable.no_image)
            }

        val factory = DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
        glideRequest
            .fallback(
                R.drawable.no_image,
            ).transition(DrawableTransitionOptions.withCrossFade(factory))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    fun setAllTypefaces(
        view: View?,
        typeface: Typeface?,
    ) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) setAllTypefaces(view.getChildAt(i), typeface)
        } else if (view is TextView) {
            view.typeface = typeface
        }
    }

    fun copyToClipboard(
        context: Context,
        text: String,
    ) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Adjust quality as needed
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    fun setClickListenerForUrl(
        context: Context,
        layout: View,
        url: String,
    ) {
        layout.setOnClickListener {
            if (url.isNotEmpty()) {
                CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
            } else {
                Log.w(javaClass.simpleName, "URL is empty")
            }
        }
    }
}
