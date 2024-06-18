package ir.masouddabbaghi.oberbox

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : MultiDexApplication() {
    companion object {
        const val BASE_URL = ""
    }
}
