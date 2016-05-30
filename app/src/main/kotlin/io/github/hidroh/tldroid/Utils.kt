package io.github.hidroh.tldroid

import okio.Okio
import java.io.IOException
import java.io.InputStream

internal object Utils {
  @Throws(IOException::class)
  fun readUtf8(inputStream: InputStream): String {
    return Okio.buffer(Okio.source(inputStream)).readUtf8()
  }
}