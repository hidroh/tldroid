package io.github.hidroh.tldroid

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import junit.framework.Assert.assertNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.*
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@PrepareForTest(Uri::class, TextUtils::class)
@RunWith(PowerMockRunner::class)
class MarkdownProcessorTest {
  @Mock internal var context: Context? = null
  @Mock internal var contentResolver: ContentResolver? = null
  @Mock internal var uri: android.net.Uri? = null
  @Mock internal var uriBuilder: android.net.Uri.Builder? = null
  @Mock internal var cursor: Cursor? = null

  @Before
  fun setUp() {
    mockStatic(Uri::class.java, TextUtils::class.java)
    `when`(uriBuilder!!.appendPath(anyString())).thenReturn(uriBuilder)
    `when`(uriBuilder!!.build()).thenReturn(mock(Uri::class.java))
    `when`(uri!!.buildUpon()).thenReturn(uriBuilder)
    `when`(Uri.parse(anyString())).thenReturn(uri)
    `when`(TextUtils.isEmpty(isNull() as CharSequence?)).thenReturn(true)
    `when`(TextUtils.isEmpty(isNotNull() as CharSequence?)).thenReturn(false)
    `when`(contentResolver!!.query(any(Uri::class.java), any(Array<String>::class.java),
        anyString(), any(Array<String>::class.java), anyString())).thenReturn(cursor)
    `when`(context!!.contentResolver).thenReturn(contentResolver)
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
}
