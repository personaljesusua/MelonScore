package vladyslavpohrebniakov.notgood.features.main

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.opencsv.CSVReader
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.db.*
import vladyslavpohrebniakov.notgood.model.AlbumArtDownloader
import vladyslavpohrebniakov.notgood.model.Common
import vladyslavpohrebniakov.notgood.model.DataBase
import vladyslavpohrebniakov.notgood.model.DataBase.Table.ALBUM_COL
import vladyslavpohrebniakov.notgood.model.DataBase.Table.ARTIST_COL
import vladyslavpohrebniakov.notgood.model.DataBase.Table.ID_COL
import vladyslavpohrebniakov.notgood.model.DataBase.Table.REVIEW_TABLE
import vladyslavpohrebniakov.notgood.model.DataBase.Table.SCORE_COL
import vladyslavpohrebniakov.notgood.model.RatingsDownloader
import vladyslavpohrebniakov.notgood.receiver.ReceiverActions
import java.io.File
import java.io.FileReader
import java.util.*


class MainPresenter(val view: MainView) {

	private var lastMusicInfo = arrayOfNulls<String>(2)
	var isDbUpdating = false

	fun init(progressText: String) {
		if (!isDbExists()) {
			isDbUpdating = true
			showProgressText(progressText)
			updateDB()
			startService()
		} else {
			isDbUpdating = false
			showLastUpdateDate()
			startService()

			showRatingFromNotification()
			val artLink = loadAlbumArtLinkFromNotification()
			showAlbumArt(artLink)
		}
	}

	fun showProgress() {
		view.setProgressGroupVisibility(true)
		view.hideLastUpdateCard()
		view.setAlbumInfoGroupVisibility(false)
	}

	fun hideProgress() {
		view.setProgressGroupVisibility(false)
		view.setAlbumInfoGroupVisibility(true)
		showLastUpdateDate()
	}

	fun showProgressText(text: String) = view.setProgressText(text)

	fun showReviewsAddedProgress(progress: Int) = view.setReviewsAddedProgress(progress)

	fun showRating(artist: String?, album: String?, rating: String?) {
		view.setRatingText(artist, album, rating)
	}

	/* Show rating when app opened form notification */
	fun showRatingFromNotification() {
		val extras = view.getExtrasFromIntent()
		showRating(extras[0], extras[1], extras[2])
	}

	fun showLastUpdateDate() {
		val lastUpdate = view.getLastUpdateDate()

		if (lastUpdate != 0L) {
			val difference = Date().time - lastUpdate
			val secondsInMilli: Long = 1000
			val minutesInMilli = secondsInMilli * 60
			val hoursInMilli = minutesInMilli * 60
			val daysInMilli = hoursInMilli * 24

			val elapsedDays = difference / daysInMilli

			view.setLastUpdateText(elapsedDays)
		} else {
			view.setLastUpdateText(-1L)
		}
	}

	/* Load album art link when app opened form notification */
	fun loadAlbumArtLinkFromNotification(): String? {
		val extras = view.getExtrasFromIntent()
		return loadAlbumArtLink(extras[0], extras[1])
	}

	fun loadAlbumArtLink(artist: String?, album: String?): String? {
		return if (artist != null && album != null) {
			return if (lastMusicInfo[0].isNullOrEmpty() || lastMusicInfo[1].isNullOrEmpty()
					|| !(lastMusicInfo[0].equals(artist) && lastMusicInfo[1].equals(album))) {
				lastMusicInfo[0] = artist
				lastMusicInfo[1] = album
				AlbumArtDownloader.getArtLink(artist, album)
			} else {
				null
			}
		} else {
			null
		}
	}

	fun searchRatingManually(artist: String, album: String) {
		view.searchRating(artist.isNotEmpty() && album.isNotEmpty(), artist, album)
	}

	fun showAlbumArt(link: String?) {
		if (!link.isNullOrEmpty())
			view.setAlbumArt(link!!)
	}

	fun showAboutDialog() = view.showAboutAppDialog()

	fun showLicensesDialog() = view.showOpenSourceLicensesDialog()

	fun setSearchCardVisibility(visible: Boolean) {
		view.setSearchCardVisibilty(visible)
	}

	fun updateDB() {
		showProgress()
		Observable.create { e: ObservableEmitter<Boolean> ->
			val downloaded = RatingsDownloader.downloadLatestCSV(view.getAppFileDir())
			if (downloaded) {
				view.saveLastUpdateDate(Date().time)
				e.onNext(true)
			} else {
				e.onNext(false)
			}
		}
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeBy(
						onNext = {
							if (!it) view.showToastConnectionError()
							else saveRatingsToDB()
						})
	}

	private fun saveRatingsToDB() {
		Observable.create { e: ObservableEmitter<Int> ->
			val database = view.getSqlHelper()
			database.use { dropTable(REVIEW_TABLE, true) }
			database.use {
				createTable(REVIEW_TABLE, true,
						ID_COL to INTEGER + PRIMARY_KEY + UNIQUE,
						ARTIST_COL to TEXT,
						ALBUM_COL to TEXT,
						SCORE_COL to TEXT)
			}

			var reader = CSVReader(FileReader(File(view.getAppFileDir(), Common.RATINGS_FILE)))
			var countReviews = 0
			while (true) {
				val nextLine = reader.readNext() ?: break
				if (nextLine.size >= 3) {
					countReviews++
				}
			}
			reader.close()
			reader = CSVReader(FileReader(File(view.getAppFileDir(), Common.RATINGS_FILE)))
			countReviews -= 1 // Skip first row because it's column titles

			var i = 0
			var prevProgress = 0
			while (true) {
				val nextLine = reader.readNext() ?: break
				if (nextLine.size >= 3) {
					if (i > 0) /* Skip first row because it's column titles */ {
						database.use {
							transaction {
								insert(REVIEW_TABLE,
										ARTIST_COL to nextLine[0],
										ALBUM_COL to nextLine[1],
										SCORE_COL to nextLine[2])
							}
						}
					}

					val percent = (i.toDouble() / countReviews.toDouble()) * 100.0
					i++
					val currentProgress = percent.toInt()
					if (prevProgress < currentProgress && currentProgress % 10 == 0) {
						prevProgress = currentProgress
						e.onNext(currentProgress)
					}
				}
			}
			reader.close()
			e.onComplete()
		}
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeBy(
						onNext = {
							showReviewsAddedProgress(it)
						},
						onComplete = {
							hideProgress()
							isDbUpdating = false
						}
				)


	}

	fun isDbExists(): Boolean {
		var checkDB: SQLiteDatabase? = null
		val databasePath = view.getDbPath(DataBase.MELON_DB_NAME)
		try {
			checkDB = SQLiteDatabase.openDatabase(databasePath, null,
					SQLiteDatabase.OPEN_READONLY)
			checkDB!!.close()
		} catch (e: SQLiteException) {
			e.printStackTrace()
		}

		return checkDB != null
	}

	fun registerBroadcastReceiver() =
			view.registerBroadcastReceiver(ReceiverActions.BROADCAST_RECEIVER_ACTIONS)


	fun unregisterBroadcastReceiver() =
			view.unregisterBroadcastReceiver()

	fun startService() {
		val isAllowed = view.getAllowForegroundService()
		view.setAllowSwitchChecked(isAllowed)
		if (isAllowed) view.startService()
	}

	fun allowForegroundService(isAllowed: Boolean) {
		view.setAllowForegroundService(isAllowed)
		if (isAllowed) view.startService()
		else view.stopService()
	}

	fun openTranslatePage() {
		view.openTranslateLink()
	}
}