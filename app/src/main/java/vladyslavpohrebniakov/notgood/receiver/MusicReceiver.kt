package vladyslavpohrebniakov.notgood.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.select
import org.jetbrains.anko.doAsync
import vladyslavpohrebniakov.notgood.MySqlHelper
import vladyslavpohrebniakov.notgood.R
import vladyslavpohrebniakov.notgood.features.main.MainActivity
import vladyslavpohrebniakov.notgood.features.main.MainPresenter
import vladyslavpohrebniakov.notgood.model.Common
import vladyslavpohrebniakov.notgood.model.DataBase

class MusicReceiver(private val presenter: MainPresenter?) : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent) {
		val extras = intent.extras
		val action = intent.action
		val cmd = extras.getString("command")
		Log.v("MusicReceiver", "$action / $cmd")


		val isPlaying = extras.getBoolean(if (extras.containsKey("playstate")) "playstate" else "playing", true)
		if (isPlaying) {
			val artist = extras.getString(Common.ARTIST_INTENT_EXTRA)
			val album = extras.getString(Common.ALBUM_INTENT_EXTRA)

			val rating = getRating(artist, album, context)
			presenter?.showRating(artist, album, rating)
			showNotification(artist, album, rating, context)

			doAsync {
				val artLink = presenter?.loadAlbumArtLink(artist, album)
				presenter?.showAlbumArt(artLink)
			}
		} else {
			dismissNotification(context)
		}
	}

	private fun getRating(artist: String?, album: String?, context: Context): String? {
		var rating: List<String>? = null
		if (artist != null && album != null) {
			val database = MySqlHelper.getInstance(context)
			rating = database.use {
				select(DataBase.Table.REVIEW_TABLE, DataBase.Table.SCORE_COL)
						.whereArgs("(${DataBase.Table.ARTIST_COL} COLLATE NOCASE = {artistName}) " +
								"and (${DataBase.Table.ALBUM_COL} COLLATE NOCASE = {albumName})",
								"artistName" to artist,
								"albumName" to album)
						.parseList(classParser())

			}
		}
		return if (rating != null && rating.isNotEmpty()) rating[0]
		else null
	}

	private fun showNotification(artist: String?, album: String?, rating: String?, context: Context) {
		val channelId = Common.NOTIFICATION_RATING_CHANNEL_ID
		val notificationId = Common.NOTIFICATION_RATING_ID
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		val notificationIntent = Intent(context, MainActivity::class.java)
		notificationIntent.putExtra(Common.ARTIST_INTENT_EXTRA, artist)
		notificationIntent.putExtra(Common.ALBUM_INTENT_EXTRA, album)
		notificationIntent.putExtra(Common.RATING_INTENT_EXTRA, rating)
		val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val importance = NotificationManager.IMPORTANCE_LOW
			val channelName = context.getString(R.string.channel_ratings_name)
			val notificationChannel = NotificationChannel(channelId, channelName, importance)
			notificationManager.createNotificationChannel(notificationChannel)
		}

		val rated = if (rating != null) context.getString(R.string.rated, album, artist, rating)
		else context.getString(R.string.no_rating)

		val notification = NotificationCompat.Builder(context, channelId)
				.setContentText(rated)
				.setSmallIcon(R.drawable.ic_notif)
				.setOngoing(false)
				.setColor(Color.YELLOW)
				.setContentIntent(pendingIntent)
				.setStyle(NotificationCompat.BigTextStyle().bigText(rated))
				.build()

		notificationManager.notify(notificationId, notification)
	}

	private fun dismissNotification(context: Context) {
		val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(Common.NOTIFICATION_RATING_ID)
	}
}