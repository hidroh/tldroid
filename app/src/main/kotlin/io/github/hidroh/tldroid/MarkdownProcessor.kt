package io.github.hidroh.tldroid

import android.content.ContentProviderOperation
import android.content.Context
import android.content.OperationApplicationException
import android.os.RemoteException
import android.support.annotation.WorkerThread
import android.text.TextUtils
import com.github.rjeschke.txtmark.Processor
import java.io.File
import java.io.IOException
import java.util.*
import java.util.zip.ZipFile

internal class MarkdownProcessor(private val platform: String) {

  companion object {
    const val ZIP_FILENAME = "tldr.zip"
    private const val COMMAND_PATH = "pages/%1\$s/%2\$s.md"
  }

  @WorkerThread
  fun process(context: Context, commandName: String, lastModified: Long): String? {
    val selection = "${TldrProvider.CommandEntry.COLUMN_NAME}=? AND " +
        "${TldrProvider.CommandEntry.COLUMN_PLATFORM}=? AND " +
        "${TldrProvider.CommandEntry.COLUMN_MODIFIED}>=?"
    val selectionArgs = arrayOf(commandName, platform, lastModified.toString())
    val cursor = context.contentResolver.query(
        TldrProvider.URI_COMMAND, null, selection, selectionArgs, null)
    val markdown: String?
    if (cursor != null && cursor.moveToFirst()) {
      markdown = cursor.getString(cursor.getColumnIndexOrThrow(
          TldrProvider.CommandEntry.COLUMN_TEXT))
    } else {
      markdown = loadFromZip(context, commandName, platform, lastModified)
    }
    cursor?.close()
    return if (TextUtils.isEmpty(markdown)) null else Processor.process(markdown)
  }

  private fun loadFromZip(context: Context, name: String, platform: String, lastModified: Long): String? {
    val markdown: String
    try {
      val zip = ZipFile(File(context.cacheDir, ZIP_FILENAME), ZipFile.OPEN_READ)
      markdown = Utils.readUtf8(zip.getInputStream(zip.getEntry(
          String.format(COMMAND_PATH, platform, name))))
      zip.close()
    } catch (e: IOException) {
      return null
    }
    persist(context, name, platform, markdown, lastModified)
    return markdown
  }

  private fun persist(context: Context, name: String, platform: String, markdown: String, lastModified: Long) {
    val operations = ArrayList<ContentProviderOperation>()
    operations.add(ContentProviderOperation.newUpdate(TldrProvider.URI_COMMAND)
        .withValue(TldrProvider.CommandEntry.COLUMN_TEXT, markdown)
        .withValue(TldrProvider.CommandEntry.COLUMN_MODIFIED, lastModified)
        .withSelection("${TldrProvider.CommandEntry.COLUMN_PLATFORM}=? AND " +
            "${TldrProvider.CommandEntry.COLUMN_NAME}=?",
            arrayOf(platform, name))
        .build())
    try {
      context.contentResolver.applyBatch(TldrProvider.AUTHORITY, operations)
    } catch (e: RemoteException) {
      // no op
    } catch (e: OperationApplicationException) {
      // no op
    }
  }
}