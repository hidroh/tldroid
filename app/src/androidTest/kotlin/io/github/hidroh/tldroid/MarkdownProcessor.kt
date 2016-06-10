package io.github.hidroh.tldroid

import android.content.Context
import android.support.annotation.WorkerThread

class MarkdownProcessor(private val platform: String?) {

  companion object {
    const val ZIP_FILENAME = "tldr.zip"
    const val COMMAND_PATH = "pages/%s/%2s.md"
    var markdown: String? = null
  }

  @WorkerThread
  fun process(context: Context, commandName: String, lastModified: Long): String? {
    return markdown
  }
}