package ir.masouddabbaghi.oberbox.data.storage

import android.content.SharedPreferences
import javax.inject.Inject

class SharedPreferencesHelperImpl
    @Inject
    constructor(
        private val sharedPreferences: SharedPreferences,
    ) : SharedPreferencesHelper {
        override fun saveString(
            key: String,
            value: String,
        ) {
            sharedPreferences.edit().putString(key, value).apply()
        }

        override fun getString(
            key: String,
            defaultValue: String,
        ): String = sharedPreferences.getString(key, defaultValue) ?: defaultValue

        override fun saveInt(
            key: String,
            value: Int,
        ) {
            sharedPreferences.edit().putInt(key, value).apply()
        }

        override fun getInt(
            key: String,
            defaultValue: Int,
        ): Int = sharedPreferences.getInt(key, defaultValue)

        override fun clearSharedPreferences() {
            sharedPreferences.edit().clear().apply()
        }
    }
