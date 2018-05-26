package vladyslavpohrebniakov.notgood

import android.content.Context
import android.preference.PreferenceManager

object SharedPrefs {
	private const val UPDATE_DATE_KEY = "update.date.key"
	private const val ALLOW_SERVICE_KEY = "allow.service.key"

	fun saveUpdateDate(context: Context, date: Long) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putLong(UPDATE_DATE_KEY, date)
				.apply()
	}

	fun getUpdateDate(context: Context): Long {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getLong(UPDATE_DATE_KEY, 0)
	}

	fun setAllowForegroundService(context: Context, allow: Boolean) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putBoolean(ALLOW_SERVICE_KEY, allow)
				.apply()
	}

	fun getAllowForegroundService(context: Context): Boolean {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(ALLOW_SERVICE_KEY, true)
	}
}