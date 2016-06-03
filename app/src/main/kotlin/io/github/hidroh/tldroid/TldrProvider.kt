package io.github.hidroh.tldroid

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.provider.BaseColumns

class TldrProvider : ContentProvider() {

  companion object {
    const val AUTHORITY = "io.github.hidroh.tldroid.provider"
    val URI_COMMAND = Uri
        .parse("content://" + AUTHORITY)
        .buildUpon()
        .appendPath(CommandEntry.TABLE_NAME)
        .build()
  }

  interface CommandEntry : BaseColumns {
    companion object {
      const val TABLE_NAME = "command"
      const val MIME_TYPE = "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE_NAME"
      const val COLUMN_NAME = "name"
      const val COLUMN_PLATFORM = "platform"
      const val COLUMN_TEXT = "text"
      const val COLUMN_MODIFIED = "modified"
    }
  }

  private var mDbHelper: DbHelper? = null

  override fun onCreate(): Boolean {
    mDbHelper = DbHelper(context)
    return true
  }

  override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                     selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
    val db = mDbHelper!!.readableDatabase
    return if (URI_COMMAND == uri) db.query(CommandEntry.TABLE_NAME,
        projection, selection, selectionArgs, null, null, null) else null
  }

  override fun getType(uri: Uri): String? {
    return if (URI_COMMAND == uri) CommandEntry.MIME_TYPE else null
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? {
    val db = mDbHelper!!.writableDatabase
    if (URI_COMMAND == uri) {
      val id = db.insert(CommandEntry.TABLE_NAME, null, values)
      return if (id == -1L) null else ContentUris.withAppendedId(URI_COMMAND, id)
    }
    return null
  }

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
    val db = mDbHelper!!.writableDatabase
    return if (URI_COMMAND == uri) db.delete(CommandEntry.TABLE_NAME,
        selection, selectionArgs) else 0
  }

  override fun update(uri: Uri, values: ContentValues?,
                      selection: String?, selectionArgs: Array<String>?): Int {
    val db = mDbHelper!!.writableDatabase
    return if (URI_COMMAND == uri) db.update(CommandEntry.TABLE_NAME,
        values, selection, selectionArgs) else 0
  }

  class DbHelper : SQLiteOpenHelper {
    companion object {
      private const val DB_NAME = "tldr.db"
      private const val DB_VERSION = 2
      private const val SQL_CREATE_COMMAND_TABLE = "CREATE TABLE ${CommandEntry.TABLE_NAME} (" +
          "${BaseColumns._ID} INTEGER PRIMARY KEY," +
          "${CommandEntry.COLUMN_NAME} TEXT," +
          "${CommandEntry.COLUMN_PLATFORM} TEXT," +
          "${CommandEntry.COLUMN_TEXT} TEXT," +
          "${CommandEntry.COLUMN_MODIFIED} INTEGER DEFAULT -1," +
          "UNIQUE (${CommandEntry.COLUMN_NAME}, ${CommandEntry.COLUMN_PLATFORM}) ON CONFLICT REPLACE)"
      private const val SQL_DROP_COMMAND_TABLE = "DROP TABLE IF EXISTS ${CommandEntry.TABLE_NAME}"
    }

    constructor(context: Context) : super(context, TldrProvider.DbHelper.DB_NAME,
        null, TldrProvider.DbHelper.DB_VERSION)

    override fun onCreate(db: SQLiteDatabase) {
      db.execSQL(SQL_CREATE_COMMAND_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      db.execSQL(SQL_DROP_COMMAND_TABLE)
      db.execSQL(SQL_CREATE_COMMAND_TABLE)
    }
  }
}
