package vladyslavpohrebniakov.notgood

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object SharedPrefs {
	private const val UPDATE_DATE_KEY = "update.date.key"
	private const val ALLOW_SERVICE_KEY = "allow.service.key"
	private var sSharedPrefs: SharedPreferences? = null

	private fun getInstanceOfSharedPrefs(context: Context): SharedPreferences {
		if (sSharedPrefs == null)
			sSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
		return sSharedPrefs!!
	}

	fun saveUpdateDate(context: Context, date: Long) {
		getInstanceOfSharedPrefs(context)
				.edit()
				.putLong(UPDATE_DATE_KEY, date)
				.apply()
	}

	fun getUpdateDate(context: Context): Long {
		return getInstanceOfSharedPrefs(context)
				.getLong(UPDATE_DATE_KEY, 0L)
	}

	fun setAllowForegroundService(context: Context, allow: Boolean) {
		getInstanceOfSharedPrefs(context)
				.edit()
				.putBoolean(ALLOW_SERVICE_KEY, allow)
				.apply()
	}

	fun getAllowForegroundService(context: Context): Boolean {
		return getInstanceOfSharedPrefs(context)
				.getBoolean(ALLOW_SERVICE_KEY, true)
	}
}