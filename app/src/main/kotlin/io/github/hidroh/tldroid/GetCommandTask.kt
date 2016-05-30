package io.github.hidroh.tldroid

import android.content.ContentProviderOperation
import android.content.OperationApplicationException
import android.os.AsyncTask
import android.os.RemoteException
import android.preference.PreferenceManager
import android.text.TextUtils
import com.github.rjeschke.txtmark.Processor
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.zip.ZipFile

internal class GetCommandTask(commandActivity: CommandActivity, private val mPlatform: String) :
    AsyncTask<String, Void, String>() {

  companion object {
    const val ZIP_FILENAME = "tldr.zip"
    private val COMMAND_PATH = "pages/%1\$s/%2\$s.md"
  }

  private val mCommandActivity: WeakReference<CommandActivity> = WeakReference(commandActivity)

  override fun doInBackground(vararg params: String): String? {
    if (mCommandActivity.get() == null) {
      return null
    }
    val lastModified = PreferenceManager.getDefaultSharedPreferences(mCommandActivity.get())
        .getLong(SyncService.PREF_LAST_ZIPPED, 0L)
    val selection = "${TldrProvider.CommandEntry.COLUMN_NAME}=? AND " +
        "${TldrProvider.CommandEntry.COLUMN_PLATFORM}=? AND " +
        "${TldrProvider.CommandEntry.COLUMN_MODIFIED}>=?"
    val selectionArgs = arrayOf(params[0], mPlatform, lastModified.toString())
    val cursor = mCommandActivity.get().contentResolver.query(
        TldrProvider.URI_COMMAND, null, selection, selectionArgs, null)
    val markdown: String?
    if (cursor != null && cursor.moveToFirst()) {
      markdown = cursor.getString(cursor.getColumnIndexOrThrow(
          TldrProvider.CommandEntry.COLUMN_TEXT))
      cursor.close()
    } else {
      markdown = loadFromZip(params[0], mPlatform, lastModified)
    }
    return if (TextUtils.isEmpty(markdown)) null else Processor.process(markdown)
  }

  override fun onPostExecute(s: String) {
    if (mCommandActivity.get() != null) {
      mCommandActivity.get().render(s)
    }
  }

  private fun loadFromZip(name: String, platform: String, lastModified: Long): String? {
    val markdown: String
    try {
      val zip = ZipFile(File(mCommandActivity.get().cacheDir, ZIP_FILENAME), ZipFile.OPEN_READ)
      markdown = Utils.readUtf8(zip.getInputStream(zip.getEntry(
          String.format(COMMAND_PATH, platform, name))))
      zip.close()
    } catch (e: IOException) {
      return null
    }
    persist(name, platform, markdown, lastModified)
    return markdown
  }

  private fun persist(name: String, platform: String, markdown: String, lastModified: Long) {
    val operations = ArrayList<ContentProviderOperation>()
    operations.add(ContentProviderOperation.newUpdate(TldrProvider.URI_COMMAND)
        .withValue(TldrProvider.CommandEntry.COLUMN_TEXT, markdown)
        .withValue(TldrProvider.CommandEntry.COLUMN_MODIFIED, lastModified)
        .withSelection("${TldrProvider.CommandEntry.COLUMN_PLATFORM}=? AND " +
            "${TldrProvider.CommandEntry.COLUMN_NAME}=?",
            arrayOf(platform, name))
        .build())
    try {
      mCommandActivity.get().contentResolver.applyBatch(TldrProvider.AUTHORITY, operations)
    } catch (e: RemoteException) {
      // no op
    } catch (e: OperationApplicationException) {
      // no op
    }

  }
}
