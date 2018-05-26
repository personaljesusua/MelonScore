package vladyslavpohrebniakov.notgood.view

import vladyslavpohrebniakov.notgood.MySqlHelper
import java.io.File

interface MainView {
	fun setProgressGroupVisibility(isVisible: Boolean)
	fun setAlbumInfoGroupVisibility(isVisible: Boolean)
	fun setProgressText(text: String)
	fun setReviewsAddedText(count: Int)
	fun setRatingText(artist: String?, album: String?, rating: String?)
	fun setLastUpdateText(elapsedDays: Long)
	fun hideLastUpdateCard()
	fun setAllowSwitchChecked(value: Boolean)
	fun setAlbumArt(link: String)
	fun showToastConnectionError()
	fun showAboutAppDialog()
	fun showOpenSourceLicensesDialog()
	fun getAppFileDir(): File
	fun getDbPath(dbName: String): String
	fun getSqlHelper(): MySqlHelper
	fun getExtrasFromIntent(): Array<String?>
	fun saveLastUpdateDate(time: Long)
	fun getLastUpdateDate(): Long
	fun setAllowForegroundService(value: Boolean)
	fun getAllowForegroundService(): Boolean
	fun registerBroadcastReceiver(actions: Array<String>)
	fun unregisterBroadcastReceiver()
	fun startService()
	fun stopService()
}