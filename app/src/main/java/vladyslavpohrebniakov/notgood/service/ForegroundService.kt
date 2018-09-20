package vladyslavpohrebniakov.notgood.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import org.jetbrains.anko.doAsync
import vladyslavpohrebniakov.notgood.R
import vladyslavpohrebniakov.notgood.model.Common
import vladyslavpohrebniakov.notgood.receiver.MusicReceiver
import vladyslavpohrebniakov.notgood.receiver.ReceiverActions

class ForegroundService : Service() {
	private var startMode: Int = 0
	private var binder: IBinder? = null
	private var allowRebind: Boolean = false
	private val musicReceiver = MusicReceiver(null)

	override fun onCreate() {
		doAsync {
			val iF = IntentFilter()
			for (action in ReceiverActions.BROADCAST_RECEIVER_ACTIONS) iF.addAction(action)
			registerReceiver(musicReceiver, iF)

			val channelId = Common.NOTIFICATION_MAIN_CHANNEL_ID
			val notificationId = Common.NOTIFICATION_MAIN_ID
			val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				val importance = NotificationManager.IMPORTANCE_MIN
				val channelName = applicationContext.getString(R.string.channel_main_name)
				val notificationChannel = NotificationChannel(channelId, channelName, importance)
				notificationManager.createNotificationChannel(notificationChannel)
			}

			val notification = NotificationCompat.Builder(applicationContext, channelId)
					.setContentText(getText(R.string.app_working))
					.setSmallIcon(R.drawable.ic_notif)
					.setOngoing(false)
					.setColor(Color.YELLOW)
					.build()

			startForeground(notificationId, notification)
		}
	}

	override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
		return startMode
	}

	override fun onBind(intent: Intent): IBinder? {
		return binder
	}

	override fun onUnbind(intent: Intent): Boolean {
		return allowRebind
	}

	override fun onDestroy() {
		try {
			unregisterReceiver(musicReceiver)
		} catch (e: IllegalArgumentException) {
			error("Receiver not registered: ${e.localizedMessage}")
		}
	}
}