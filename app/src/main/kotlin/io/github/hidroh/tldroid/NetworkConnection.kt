package io.github.hidroh.tldroid

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class NetworkConnection(url: String) {
  private var connection: HttpURLConnection?
  private var inputStream: InputStream? = null

  init {
    connection = try { URL(url).openConnection() as HttpURLConnection? } catch (e: IOException) { null }
  }

  fun connect() {
    try { connection?.connect() } catch (e: IOException) { }
  }

  fun disconnect() {
    try { inputStream?.close() } catch (e: IOException) { }
    connection?.disconnect()
  }

  fun getResponseCode(): Int {
    return try { connection?.responseCode ?: 0 } catch (e: IOException) { 0 }
  }

  fun getInputStream(): InputStream? {
    inputStream = try { connection?.inputStream ?: null } catch (e: IOException) { null }
    return inputStream
  }

  fun setIfModifiedSince(ifModifiedSince: Long) {
    connection?.ifModifiedSince = ifModifiedSince
  }

  fun getLastModified(): Long {
    return connection?.lastModified ?: 0L
  }
}