package io.github.hidroh.tldroid

import android.os.AsyncTask
import android.preference.PreferenceManager
import java.lang.ref.WeakReference

internal class GetCommandTask(commandActivity: CommandActivity, platform: String?) :
    AsyncTask<String, Void, String>() {

  private val commandActivity: WeakReference<CommandActivity> = WeakReference(commandActivity)
  private val processor : MarkdownProcessor = MarkdownProcessor(platform)

  override fun doInBackground(vararg params: String): String? {
    val context = commandActivity.get() ?: return null
    val commandName = params[0]
    val lastModified = PreferenceManager.getDefaultSharedPreferences(context)
        .getLong(SyncService.PREF_LAST_ZIPPED, 0L)
    return processor.process(context, commandName, lastModified)
  }

  override fun onPostExecute(s: String?) {
    if (commandActivity.get() != null) {
      commandActivity.get().render(s)
    }
  }


}
