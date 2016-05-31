package io.github.hidroh.tldroid

import android.app.IntentService
import android.content.ContentProviderOperation
import android.content.Intent
import android.content.OperationApplicationException
import android.os.RemoteException
import android.preference.PreferenceManager
import android.util.Log
import com.squareup.moshi.Moshi
import okio.Okio
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SyncService : IntentService(SyncService.TAG) {
  companion object {
    private const val TAG = "SyncService"
    private const val INDEX_URL = "http://tldr-pages.github.io/assets/index.json"
    private const val ZIP_URL = "http://tldr-pages.github.io/assets/tldr.zip"
    const val EXTRA_ASSET_TYPE = "$TAG.EXTRA_ASSET_TYPE"
    const val PREF_LAST_REFRESHED = INDEX_URL
    const val PREF_LAST_ZIPPED = ZIP_URL
    const val PREF_COMMAND_COUNT = "PREF_COMMAND_COUNT"
    const val ASSET_TYPE_INDEX = 0
    const val ASSET_TYPE_ZIP = 1
  }

  override fun onHandleIntent(intent: Intent) {
    if (intent.getIntExtra(EXTRA_ASSET_TYPE, ASSET_TYPE_INDEX) == ASSET_TYPE_INDEX) {
      syncIndex()
    } else {
      syncZip()
    }
  }

  private fun syncIndex() {
    val connection = connect(INDEX_URL) ?: return
    try {
      persist(Moshi.Builder()
          .build()
          .adapter(Commands::class.java)
          .fromJson(Utils.readUtf8(connection.inputStream)))
    } catch (e: IOException) {
      Log.e(TAG, e.toString())
    } finally {
      connection.disconnect()
    }
  }

  private fun syncZip() {
    val connection = connect(ZIP_URL) ?: return
    try {
      val sink = Okio.buffer(Okio.sink(File(cacheDir, MarkdownProcessor.ZIP_FILENAME)))
      sink.writeAll(Okio.source(connection.inputStream))
      sink.close()
    } catch (e: IOException) {
      Log.e(TAG, e.toString())
    } finally {
      connection.disconnect()
    }
  }

  private fun connect(url: String): HttpURLConnection? {
    val connection: HttpURLConnection
    try {
      connection = URL(url).openConnection() as HttpURLConnection
    } catch (e: IOException) {
      Log.e(TAG, e.toString())
      return null
    }

    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
    connection.ifModifiedSince = sharedPrefs.getLong(url, 0L)
    try {
      connection.connect()
      if (connection.responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
        connection.disconnect()
        return null
      }
    } catch (e: IOException) {
      Log.e(TAG, e.toString())
      return null
    }

    sharedPrefs.edit().putLong(url, connection.lastModified).apply()
    return connection
  }

  private fun persist(commands: Commands) {
    if (commands.commands == null || commands.commands!!.size == 0) {
      return
    }
    PreferenceManager.getDefaultSharedPreferences(this)
        .edit()
        .putInt(PREF_COMMAND_COUNT, commands.commands!!.size)
        .apply()
    val operations = ArrayList<ContentProviderOperation>()
    for (command in commands.commands!!) {
      for (platform in command.platform!!) {
        operations.add(ContentProviderOperation.newInsert(TldrProvider.URI_COMMAND)
            .withValue(TldrProvider.CommandEntry.COLUMN_NAME, command.name)
            .withValue(TldrProvider.CommandEntry.COLUMN_PLATFORM, platform)
            .build())
      }
    }
    val cr = contentResolver
    try {
      cr.applyBatch(TldrProvider.AUTHORITY, operations)
      cr.notifyChange(TldrProvider.URI_COMMAND, null)
    } catch (e: RemoteException) {
      // no op
    } catch (e: OperationApplicationException) {
    }

  }

  private class Commands {
    internal var commands: Array<Command>? = null
  }

  private class Command {
    internal var name: String? = null
    internal var platform: Array<String>? = null
  }
}
