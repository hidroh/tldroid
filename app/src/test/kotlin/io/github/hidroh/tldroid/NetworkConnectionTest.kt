package io.github.hidroh.tldroid

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NetworkConnectionTest {
  private val server: MockWebServer = MockWebServer()

  @Before
  fun setUp() {
    server.start()
  }

  @Test
  fun testGetResponseCode() {
    server.enqueue(MockResponse().setResponseCode(200))
    val connection = NetworkConnection(server.url("/index.json").toString())
    connection.connect()
    Assertions.assertThat(connection.getResponseCode()).isEqualTo(200)
    connection.disconnect()
  }

  @Test
  fun testGetInputStream() {
    server.enqueue(MockResponse().setBody("{}"))
    val connection = NetworkConnection(server.url("/index.json").toString())
    connection.connect()
    Assertions.assertThat(Okio.buffer(Okio.source(connection.getInputStream())).readUtf8())
        .isEqualTo("{}")
    connection.disconnect()
  }

  @Test
  fun testNoInputStream() {
    server.enqueue(MockResponse().setResponseCode(404))
    val connection = NetworkConnection(server.url("/index.json").toString())
    connection.connect()
    Assertions.assertThat(connection.getInputStream()).isNull()
    connection.disconnect()
  }

  @Test
  fun testModified() {
    server.enqueue(MockResponse().setResponseCode(304).setHeader("Last-Modified",
        "Fri, 03 Jun 2016 17:11:33 GMT"))
    val connection = NetworkConnection(server.url("/index.json").toString())
    connection.setIfModifiedSince(0L)
    connection.connect()
    Assertions.assertThat(connection.getLastModified()).isEqualTo(1464973893000L)
    connection.disconnect()
  }

  @Test
  fun testMissingConnect() {
    val connection = NetworkConnection("/index.json")
    connection.setIfModifiedSince(0L) // should ignore
    Assertions.assertThat(connection.getResponseCode()).isEqualTo(0)
    Assertions.assertThat(connection.getInputStream()).isNull()
    Assertions.assertThat(connection.getLastModified()).isEqualTo(0L)
    connection.disconnect() // should fail silently
  }

  @After
  fun tearDown() {
    server.shutdown()
  }
}