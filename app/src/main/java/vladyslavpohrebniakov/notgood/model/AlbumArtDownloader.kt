package vladyslavpohrebniakov.notgood.model

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder


object AlbumArtDownloader {

	fun getArtLink(artist: String, album: String): String? {
		val artistQuery = URLEncoder.encode(artist, "utf-8")
		val albumQuery = URLEncoder.encode(album, "utf-8")
		val key = ApiKeys.LAST_FM_KEY
		val url = "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=$key&artist=$artistQuery&album=$albumQuery&format=json"

		Log.d("AlbumArtDownloader", url)

		val client = OkHttpClient()
		val request = Request.Builder()
				.url(url)
				.build()

		val response = try {
			client.newCall(request).execute()
		} catch (e: Exception) {
			return null
		}

		if (!response.isSuccessful) throw IOException("Unexpected code $response")
		val root = JSONObject(response.body()?.string())
		return if (root.has("album")) {
			val albumObg = root.getJSONObject("album")
			val imageArray = albumObg.getJSONArray("image")
			val albumArt = imageArray.getJSONObject(imageArray.length() - 1)
			albumArt.getString("#text")
		} else {
			null
		}
	}

}