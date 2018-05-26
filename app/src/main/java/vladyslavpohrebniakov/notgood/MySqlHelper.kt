package vladyslavpohrebniakov.notgood

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import vladyslavpohrebniakov.notgood.model.DataBase.MELON_DB_NAME
import vladyslavpohrebniakov.notgood.model.DataBase.Table.ALBUM_COL
import vladyslavpohrebniakov.notgood.model.DataBase.Table.ARTIST_COL
import vladyslavpohrebniakov.notgood.model.DataBase.Table.ID_COL
import vladyslavpohrebniakov.notgood.model.DataBase.Table.REVIEW_TABLE
import vladyslavpohrebniakov.notgood.model.DataBase.Table.SCORE_COL

class MySqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, MELON_DB_NAME) {

	companion object {
		private var instance: MySqlHelper? = null

		@Synchronized
		fun getInstance(ctx: Context): MySqlHelper {
			if (instance == null)
				instance = MySqlHelper(ctx.applicationContext)
			return instance!!
		}
	}

	override fun onCreate(db: SQLiteDatabase) {
		db.createTable(REVIEW_TABLE, true,
				ID_COL to INTEGER + PRIMARY_KEY + UNIQUE,
				ARTIST_COL to TEXT,
				ALBUM_COL to TEXT,
				SCORE_COL to TEXT)
	}

	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		db.dropTable(REVIEW_TABLE, true)
	}

}

val Context.database: MySqlHelper
	get() = MySqlHelper.getInstance(applicationContext)