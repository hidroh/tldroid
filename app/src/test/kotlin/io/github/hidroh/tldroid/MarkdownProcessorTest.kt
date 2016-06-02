package io.github.hidroh.tldroid

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import junit.framework.Assert.assertNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@PrepareForTest(Uri::class, TextUtils::class, ContentProviderOperation::class)
@RunWith(PowerMockRunner::class)
class MarkdownProcessorTest {
  @Mock internal var context: Context? = null
  @Mock internal var contentResolver: ContentResolver? = null
  @Mock internal var uri: android.net.Uri? = null
  @Mock internal var uriBuilder: android.net.Uri.Builder? = null
  @Mock internal var cursor: Cursor? = null
  @Mock internal val opBuilder: ContentProviderOperation.Builder? = null

  @Before
  fun setUp() {
    mockStatic(Uri::class.java, TextUtils::class.java, ContentProviderOperation::class.java)
    `when`(uriBuilder!!.appendPath(anyString())).thenReturn(uriBuilder)
    `when`(uriBuilder!!.build()).thenReturn(mock(Uri::class.java))
    `when`(uri!!.buildUpon()).thenReturn(uriBuilder)
    `when`(Uri.parse(anyString())).thenReturn(uri)
    `when`(TextUtils.isEmpty(isNull() as CharSequence?)).thenReturn(true)
    `when`(TextUtils.isEmpty(isNotNull() as CharSequence?)).thenReturn(false)
    `when`(ContentProviderOperation.newUpdate(any(Uri::class.java))).thenReturn(opBuilder)
    `when`(opBuilder!!.withValue(anyString(), any(ContentValues::class.java))).thenReturn(opBuilder)
    `when`(opBuilder.withSelection(anyString(), any(Array<String>::class.java))).thenReturn(opBuilder)
    `when`(opBuilder.build()).thenReturn(mock(ContentProviderOperation::class.java))
    `when`(contentResolver!!.query(any(Uri::class.java), any(Array<String>::class.java),
        anyString(), any(Array<String>::class.java), anyString())).thenReturn(cursor)
    `when`(context!!.contentResolver).thenReturn(contentResolver)
    createZip("common", "ls", "## Heading")
  }

  @Test
  fun testProcessEmptyData() {
    `when`(cursor!!.moveToFirst()).thenReturn(false)
    assertNull(MarkdownProcessor("osx").process(context!!, "ls", 0))
  }

  @Test
  fun testProcessWithSqlData() {
    `when`(cursor!!.moveToFirst()).thenReturn(true)
    `when`(cursor!!.getString(anyInt())).thenReturn("## Heading")
    assertThat(MarkdownProcessor("osx").process(context!!, "ls", 0)).contains("<h2>Heading</h2>")
  }

  @Test
  fun testLoadFromZip() {
    `when`(cursor!!.moveToFirst()).thenReturn(false)
    assertThat(MarkdownProcessor("common").process(context!!, "ls", 0)).contains("<h2>Heading</h2>")
    verify(contentResolver!!).applyBatch(anyString(),
        any(ArrayList::class.java as Class<ArrayList<ContentProviderOperation>>))
  }

  @Test
  fun testProcessDerivePlatform() {
    `when`(cursor!!.moveToFirst()).thenReturn(false, true)
    `when`(cursor!!.getString(anyInt())).thenReturn("common")
    assertThat(MarkdownProcessor(null).process(context!!, "ls", 0)).contains("<h2>Heading</h2>")
  }

  @Test
  fun testProcessNoPlatform() {
    `when`(cursor!!.moveToFirst()).thenReturn(false, false)
    assertNull(MarkdownProcessor(null).process(context!!, "ls", 0))
  }

  private fun createZip(platform: String, command: String, content: String) {
    val entry = MarkdownProcessor.COMMAND_PATH.format(platform, command)
    val outStream = ZipOutputStream(FileOutputStream(File(context!!.cacheDir,
        MarkdownProcessor.ZIP_FILENAME)))
    outStream.putNextEntry(ZipEntry(entry))
    outStream.write(content.toByteArray())
    outStream.closeEntry()
    outStream.close()
  }

  @After
  fun tearDown() {
    File(context!!.cacheDir, MarkdownProcessor.ZIP_FILENAME).delete()
  }
}
