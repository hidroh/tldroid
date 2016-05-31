package io.github.hidroh.tldroid

import java.io.IOException
import java.io.InputStream

class NetworkConnection(url: String) {
  private var responseCode: Int = 0
  private var lastModified: Long = 0L
  private var response: InputStream? = null

  fun connect() {
  }

  fun disconnect() {
    try { response?.close() } catch (e: IOException) { }
  }

  fun getResponseCode(): Int {
    return responseCode
  }

  fun getInputStream(): InputStream? {
    return response
  }

  fun setIfModifiedSince(ifModifiedSince: Long) {
  }

  fun getLastModified(): Long {
    return lastModified
  }

  fun mockResponse(responseCode: Int, lastModified: Long, response: InputStream) {
    this.responseCode = responseCode
    this.lastModified = lastModified
    this.response = response
  }
}