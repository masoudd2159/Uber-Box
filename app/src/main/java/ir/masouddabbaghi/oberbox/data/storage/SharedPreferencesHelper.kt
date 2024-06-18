package ir.masouddabbaghi.oberbox.data.storage

interface SharedPreferencesHelper {
    fun saveString(
        key: String,
        value: String,
    )

    fun getString(
        key: String,
        defaultValue: String,
    ): String

    fun saveInt(
        key: String,
        value: Int,
    )

    fun getInt(
        key: String,
        defaultValue: Int,
    ): Int

    fun clearSharedPreferences()
}
