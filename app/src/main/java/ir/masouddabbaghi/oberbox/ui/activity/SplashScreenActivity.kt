package ir.masouddabbaghi.oberbox.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import ir.masouddabbaghi.oberbox.R
import ir.masouddabbaghi.oberbox.data.storage.SharedPreferencesHelper
import ir.masouddabbaghi.oberbox.data.storage.SharedPreferencesKeys
import ir.masouddabbaghi.oberbox.databinding.ActivitySplashScreenBinding
import ir.masouddabbaghi.oberbox.ui.base.BaseActivity
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val splashDelay: Long = 2_000L // 2 seconds

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun getLayoutResourceBinding(): View {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.version.text = getString(R.string.app_version, packageManager.getPackageInfo(packageName, 0).versionName)
        if (sharedPreferencesHelper.getString(SharedPreferencesKeys.KEY_ACCESS_TOKEN, "").isEmpty()) {
            sharedPreferencesHelper.clearSharedPreferences()
            Handler(Looper.getMainLooper()).postDelayed({
//                val intent = Intent(this@SplashScreenActivity, AuthenticationActivity::class.java)
                val intent = Intent(this@SplashScreenActivity, MapsActivity::class.java)
                startActivity(intent)
                finish() // Finish the SplashScreenActivity and Start AuthenticationActivity
            }, splashDelay)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashScreenActivity, MapsActivity::class.java)
                startActivity(intent)
                finish() // Finish the SplashScreenActivity and Start MapsActivity
            }, splashDelay)
        }
    }
}
