package vladyslavpohrebniakov.notgood.features.main

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import com.squareup.picasso.Picasso
import de.psdev.licensesdialog.LicensesDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_card.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import vladyslavpohrebniakov.notgood.Animation
import vladyslavpohrebniakov.notgood.MySqlHelper
import vladyslavpohrebniakov.notgood.R
import vladyslavpohrebniakov.notgood.SharedPrefs
import vladyslavpohrebniakov.notgood.model.Common
import vladyslavpohrebniakov.notgood.receiver.MusicReceiver
import vladyslavpohrebniakov.notgood.service.ForegroundService
import java.io.File


class MainActivity : AppCompatActivity(), MainView {

	private lateinit var musicReceiver: MusicReceiver
	private lateinit var presenter: MainPresenter
	private var animation: Animation? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		presenter = MainPresenter(this)
		musicReceiver = MusicReceiver(presenter)

		doAsync {
			presenter.init(getString(R.string.updating_db))
		}

		updateButton.setOnClickListener {
			doAsync {
				presenter.showProgressText(getString(R.string.updating_db))
				presenter.updateDB()
			}
		}
		allowSwitch.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
			presenter.allowForegroundService(b)
		}
		closeSearchBtn.setOnClickListener {
			presenter.setSearchCardVisibility(false)
		}
		searchBtn.setOnClickListener {
			presenter.searchRatingManually(artistEditText.text.toString(), albumEditText.text.toString())
		}
	}

	override fun onResume() {
		super.onResume()
		presenter.registerBroadcastReceiver()
	}

	override fun onPause() {
		super.onPause()
		presenter.unregisterBroadcastReceiver()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu_main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		val id = item?.itemId

		return when (id) {
			R.id.about -> {
				presenter.showAboutDialog()
				true
			}
			R.id.search -> {
				if (!presenter.isDbUpdating) {
					presenter.setSearchCardVisibility(true)
				}
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun setProgressGroupVisibility(isVisible: Boolean) =
			runOnUiThread { progressGroup.visibility = if (isVisible) View.VISIBLE else View.GONE }

	override fun setAlbumInfoGroupVisibility(isVisible: Boolean) =
			runOnUiThread { albumInfoGroup.visibility = if (isVisible) View.VISIBLE else View.GONE }

	override fun setProgressText(text: String) =
			runOnUiThread { progressTextView.text = text }

	override fun setReviewsAddedText(count: Int) =
			runOnUiThread { reviewsAddedTextView.text = getString(R.string.reviews_added, count) }

	override fun setRatingText(artist: String?, album: String?, rating: String?) {
		ratingTextView.text = if (rating != null) getString(R.string.rated, album, artist, rating)
		else getString(R.string.no_rating)
	}

	override fun setLastUpdateText(elapsedDays: Long) {
		runOnUiThread {
			if (lastUpdateCardView.visibility == View.GONE) lastUpdateCardView.visibility = View.VISIBLE
			lastUpdateTextView.text = getString(R.string.last_update, elapsedDays)
		}
	}

	override fun hideLastUpdateCard() =
			runOnUiThread { lastUpdateCardView.visibility = View.GONE }

	override fun setAllowSwitchChecked(value: Boolean) =
			runOnUiThread { allowSwitch.isChecked = value }

	override fun setAlbumArt(link: String) =
			runOnUiThread { Picasso.get().load(link).placeholder(R.mipmap.ic_launcher).into(albumArt) }


	override fun showToastConnectionError() =
			runOnUiThread { toast(R.string.check_internet).duration = Toast.LENGTH_LONG }

	override fun showAboutAppDialog() {
		alert(getString(R.string.about_message), getString(R.string.about)
		) {
			positiveButton(android.R.string.ok, onClicked = {})
			neutralPressed(R.string.open_source_licenses, onClicked = { presenter.showLicensesDialog() })
		}.show()
	}

	override fun showOpenSourceLicensesDialog() {
		LicensesDialog.Builder(this)
				.setNotices(R.raw.notices)
				.setIncludeOwnLicense(true)
				.setTitle(R.string.open_source_licenses)
				.build()
				.show()
	}

	override fun setSearchCardVisibilty(visible: Boolean) {
		// show through animation
		if (animation == null)
			animation = Animation(rootView, this, R.layout.activity_main_search_showed)
		animation?.animateWithConstraints(visible)
		with(artistEditText) {
			if (visible) {
				if (text.isNotEmpty())
					text.clear()
				clearFocus()
			}
		}
		with(albumEditText) {
			if (visible) {
				if (text.isNotEmpty())
					text.clear()
				clearFocus()
			}
		}
	}

	override fun searchRating(canSearch: Boolean, artist: String, album: String) {
		if (canSearch) {
			val rating = MySqlHelper.getRating(artist, album, this)
			presenter.showRating(artist, album, rating)

			doAsync {
				val artLink = presenter.loadAlbumArtLink(artist, album)
				presenter.showAlbumArt(artLink)
			}
			presenter.setSearchCardVisibility(false)
		} else {
			toast(R.string.enter_all_fields)
		}
	}

	override fun getAppFileDir(): File {
		return applicationContext.filesDir
	}

	override fun getDbPath(dbName: String): String {
		return applicationContext.getDatabasePath(dbName).path
	}

	override fun getSqlHelper(): MySqlHelper {
		return MySqlHelper.getInstance(this)
	}

	override fun getExtrasFromIntent(): Array<String?> {
		val artist = intent.getStringExtra(Common.ARTIST_INTENT_EXTRA)
		val album = intent.getStringExtra(Common.ALBUM_INTENT_EXTRA)
		val rating = intent.getStringExtra(Common.RATING_INTENT_EXTRA)

		return arrayOf(artist, album, rating)
	}

	override fun saveLastUpdateDate(time: Long) =
			SharedPrefs.saveUpdateDate(this, time)

	override fun getLastUpdateDate(): Long {
		return SharedPrefs.getUpdateDate(this)
	}

	override fun setAllowForegroundService(value: Boolean) =
			SharedPrefs.setAllowForegroundService(this, value)

	override fun getAllowForegroundService(): Boolean =
			SharedPrefs.getAllowForegroundService(this)

	override fun registerBroadcastReceiver(actions: Array<String>) {
		val iF = IntentFilter()
		for (action in actions) iF.addAction(action)
		registerReceiver(musicReceiver, iF)
	}

	override fun unregisterBroadcastReceiver() =
			unregisterReceiver(musicReceiver)

	override fun startService() {
		startService(Intent(this, ForegroundService::class.java))
	}

	override fun stopService() {
		stopService(Intent(this, ForegroundService::class.java))
	}
}