package vladyslavpohrebniakov.notgood.model

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.io.File
import java.io.IOException


object RatingsDownloader {

	fun downloadLatestCSV(fileToSave: File): Boolean {
		val client = OkHttpClient()

		val request = Request.Builder()
				.url(Common.MELON_SCORES_CSV_LINK)
				.build()

		return try {
			val response = client.newCall(request).execute()
			if (!response.isSuccessful) throw IOException("Unexpected code $response")

			val sink = Okio.buffer(Okio.sink(File(fileToSave, Common.RATINGS_FILE)))
			response.body()?.let {
				sink.writeAll(it.source())
			}
			sink.close()
			true
		} catch (e: Exception) {
			e.printStackTrace()
			false
		}
	}
}