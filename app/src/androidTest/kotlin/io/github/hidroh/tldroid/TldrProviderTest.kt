package io.github.hidroh.tldroid

import android.content.ContentValues
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ProviderTestCase2
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TldrProviderTest: ProviderTestCase2<TldrProvider>(TldrProvider::class.java, TldrProvider.AUTHORITY) {

  @Before
  public override fun setUp() {
    context = InstrumentationRegistry.getTargetContext()
    super.setUp()
    val cv = ContentValues()
    cv.put(TldrProvider.CommandEntry.COLUMN_NAME, "ls")
    cv.put(TldrProvider.CommandEntry.COLUMN_PLATFORM, "common")
    cv.put(TldrProvider.CommandEntry.COLUMN_TEXT, "## Heading")
    mockContentResolver.insert(TldrProvider.URI_COMMAND, cv)
  }

  @Test
  fun testGetType() {
    assertEquals(TldrProvider.CommandEntry.MIME_TYPE, mockContentResolver
        .getType(TldrProvider.URI_COMMAND))
  }

  @Test
  fun testQuery() {
    val actual = mockContentResolver.query(TldrProvider.URI_COMMAND, null, null, null, null)
    assertThat(actual.count).isEqualTo(1)
    assertTrue(actual.moveToFirst())
    assertThat(actual.getString(actual.getColumnIndex(TldrProvider.CommandEntry.COLUMN_NAME)))
        .isEqualTo("ls")
    assertThat(actual.getString(actual.getColumnIndex(TldrProvider.CommandEntry.COLUMN_PLATFORM)))
        .isEqualTo("common")
    assertThat(actual.getString(actual.getColumnIndex(TldrProvider.CommandEntry.COLUMN_TEXT)))
        .isEqualTo("## Heading")
    actual?.close()
  }

  @Test
  fun testInsertReplace() {
    val cv = ContentValues()
    cv.put(TldrProvider.CommandEntry.COLUMN_NAME, "ls")
    cv.put(TldrProvider.CommandEntry.COLUMN_PLATFORM, "common")
    cv.put(TldrProvider.CommandEntry.COLUMN_TEXT, "## Update")
    mockContentResolver.insert(TldrProvider.URI_COMMAND, cv)
    val actual = mockContentResolver.query(TldrProvider.URI_COMMAND,
        null, null, null, null)
    assertThat(actual.count).isEqualTo(1)
    assertTrue(actual.moveToFirst())
    assertThat(actual.getString(actual.getColumnIndex(TldrProvider.CommandEntry.COLUMN_TEXT)))
        .isEqualTo("## Update")
    actual?.close()
  }

  @Test
  fun testUpdate() {
    val updateCv = ContentValues()
    updateCv.put(TldrProvider.CommandEntry.COLUMN_TEXT, "## Update")
    mockContentResolver.update(TldrProvider.URI_COMMAND, updateCv,
        "${TldrProvider.CommandEntry.COLUMN_NAME}=?", arrayOf("ls"))
    val actual = mockContentResolver.query(TldrProvider.URI_COMMAND, null, null, null, null)
    assertTrue(actual.moveToFirst())
    assertThat(actual.getString(actual.getColumnIndex(TldrProvider.CommandEntry.COLUMN_TEXT)))
        .isEqualTo("## Update")
    actual?.close()
  }

  @Test
  fun testDelete() {
    mockContentResolver.delete(TldrProvider.URI_COMMAND, null, null)
    val actual = mockContentResolver.query(TldrProvider.URI_COMMAND, null, null, null, null)
    assertThat(actual.count).isEqualTo(0)
    actual?.close()
  }
}