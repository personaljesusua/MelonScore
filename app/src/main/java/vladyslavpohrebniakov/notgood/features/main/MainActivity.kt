package vladyslavpohrebniakov.notgood.features.main

import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import de.psdev.licensesdialog.LicensesDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.search_card.*
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
			presenter.showProgressText(getString(R.string.updating_db))
			presenter.updateDB()
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
			R.id.translate -> {
				presenter.openTranslatePage()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	override fun setProgressGroupVisibility(isVisible: Boolean) {
		progressGroup.visibility = if (isVisible) View.VISIBLE else View.GONE
	}

	override fun setAlbumInfoGroupVisibility(isVisible: Boolean) =
			runOnUiThread { albumInfoGroup.visibility = if (isVisible) View.VISIBLE else View.GONE }

	override fun setProgressText(text: String) =
			runOnUiThread { progressTextView.text = text }

	override fun setReviewsAddedProgress(progress: Int) {
		progressBar.progress = progress
	}

	override fun setRatingText(artist: String?, album: String?, rating: String?) {
		ratingTextView.text = if (rating != null) getString(R.string.rated, album, artist, rating)
		else getString(R.string.no_rating)
	}

	override fun setLastUpdateText(elapsedDays: Long) {
		runOnUiThread {
			if (lastUpdateCardView.visibility == View.GONE) lastUpdateCardView.visibility = View.VISIBLE
			lastUpdateTextView.text =
					if (elapsedDays != -1L)
						getString(R.string.last_update, elapsedDays)
					else
						getString(R.string.db_need_update)
		}
	}

	override fun hideLastUpdateCard() =
			runOnUiThread { lastUpdateCardView.visibility = View.GONE }

	override fun setAllowSwitchChecked(value: Boolean) =
			runOnUiThread { allowSwitch.isChecked = value }

	override fun setAlbumArt(link: String) =
			runOnUiThread { Picasso.get().load(link).placeholder(R.mipmap.ic_launcher).into(albumArt) }


	override fun showToastConnectionError() =
			runOnUiThread {
				try {
					toast(R.string.check_internet).duration = Toast.LENGTH_LONG
				} catch (_: Exception) {
				}
			}

	override fun showAboutAppDialog() {
		AlertDialog.Builder(this)
				.setTitle(R.string.about)
				.setMessage(R.string.about_message)
				.setPositiveButton(android.R.string.ok, null)
				.setNeutralButton(R.string.open_source_licenses) { _: DialogInterface, _: Int ->
					presenter.showLicensesDialog()
				}
				.show()
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
		artistInputLayout.error = null
		albumInputLayout.error = null

		with(artistEditText) {
			if (visible) {
				text?.let {
					if (it.isNotEmpty())
						it.clear()
					clearFocus()
				}
			}
		}
		with(albumEditText) {
			if (visible) {
				text?.let {
					if (it.isNotEmpty())
						it.clear()
					clearFocus()
				}
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
			if (artist.isEmpty()) artistInputLayout.error = getString(R.string.field_cant_be_empty)
			if (album.isEmpty()) albumInputLayout.error = getString(R.string.field_cant_be_empty)
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

	override fun openTranslateLink() {
		val intent = Intent(ACTION_VIEW, Uri.parse(Common.TRANSLATE_LINK))
		startActivity(intent)
	}
}