package io.github.hidroh.tldroid

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SyncServiceTest {

  @Before
  fun setUp() {
    InstrumentationRegistry.getTargetContext().contentResolver
        .delete(TldrProvider.URI_COMMAND, null, null)
    File(InstrumentationRegistry.getTargetContext().cacheDir,
        MarkdownProcessor.ZIP_FILENAME).delete()
  }

  @Test
  fun testSyncIndex() {
    NetworkConnection.mockResponse(200, System.currentTimeMillis(),
        "{\"commands\": [{\"name\": \"ls\", \"platform\": [\"common\"]}]}".byteInputStream())
    SyncService.NetworkSync(InstrumentationRegistry.getTargetContext()).syncIndex()
    val cursor = InstrumentationRegistry.getTargetContext().contentResolver
        .query(TldrProvider.URI_COMMAND, null, null, null, null)
    assertThat(cursor.count).isEqualTo(1)
    cursor?.close()
  }

  @Test
  fun testSyncIndexInvalidJson() {
    NetworkConnection.mockResponse(200, System.currentTimeMillis(), "[]".byteInputStream())
    SyncService.NetworkSync(InstrumentationRegistry.getTargetContext()).syncIndex()
    val cursor = InstrumentationRegistry.getTargetContext().contentResolver
        .query(TldrProvider.URI_COMMAND, null, null, null, null)
    assertThat(cursor.count).isEqualTo(0)
    cursor?.close()
  }

  @Test
  fun testSyncIndexNotModified() {
    NetworkConnection.mockResponse(304, System.currentTimeMillis(),
        "{\"commands\": [{\"name\": \"ls\", \"platform\": [\"common\"]}]}".byteInputStream())
    SyncService.NetworkSync(InstrumentationRegistry.getTargetContext()).syncIndex()
    val cursor = InstrumentationRegistry.getTargetContext().contentResolver
        .query(TldrProvider.URI_COMMAND, null, null, null, null)
    assertThat(cursor.count).isEqualTo(0)
    cursor?.close()
  }

  @Test
  fun testSyncZip() {
    NetworkConnection.mockResponse(200, System.currentTimeMillis(), "".byteInputStream())
    SyncService.NetworkSync(InstrumentationRegistry.getTargetContext()).syncZip()
    assertThat(File(InstrumentationRegistry.getTargetContext().cacheDir,
        MarkdownProcessor.ZIP_FILENAME)).exists()
  }

  @Test
  fun testSyncZipNoStream() {
    NetworkConnection.mockResponse(200, System.currentTimeMillis(), null)
    SyncService.NetworkSync(InstrumentationRegistry.getTargetContext()).syncZip()
    assertThat(File(InstrumentationRegistry.getTargetContext().cacheDir,
        MarkdownProcessor.ZIP_FILENAME)).doesNotExist()
  }

  @Test
  fun testSyncNotModified() {
    NetworkConnection.mockResponse(304, System.currentTimeMillis(), "".byteInputStream())
    SyncService.NetworkSync(InstrumentationRegistry.getTargetContext()).syncZip()
    assertThat(File(InstrumentationRegistry.getTargetContext().cacheDir,
        MarkdownProcessor.ZIP_FILENAME)).doesNotExist()
  }

  @After
  fun tearDown() {
    NetworkConnection.mockResponse(0, 0L, null)
  }
}