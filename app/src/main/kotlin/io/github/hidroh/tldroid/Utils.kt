package io.github.hidroh.tldroid

import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.StyleRes
import okio.Okio
import java.io.IOException
import java.io.InputStream

internal object Utils {
  private val KEY_THEME: String = "pref:theme"
  private val VAL_THEME_SOLARIZED: String = "theme:solarized"
  private val VAL_THEME_AFTERGLOW: String = "theme:afterglow"
  private val VAL_THEME_TOMORROW: String = "theme:tomorrow"

  @Throws(IOException::class)
  fun readUtf8(inputStream: InputStream): String {
    return Okio.buffer(Okio.source(inputStream)).readUtf8()
  }

  fun saveTheme(context: Context, @StyleRes themeRes: Int) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .edit()
        .putString(KEY_THEME, when (themeRes) {
          R.style.AppTheme_Afterglow -> VAL_THEME_AFTERGLOW
          R.style.AppTheme_Tomorrow -> VAL_THEME_TOMORROW
          else -> VAL_THEME_SOLARIZED
        })
        .apply()
  }

  @StyleRes
  fun loadTheme(context: Context): Int {
    val theme = PreferenceManager.getDefaultSharedPreferences(context)
        .getString(KEY_THEME, VAL_THEME_SOLARIZED)
    when (theme) {
      VAL_THEME_AFTERGLOW -> return R.style.AppTheme_Afterglow
      VAL_THEME_TOMORROW -> return R.style.AppTheme_Tomorrow
      else -> return R.style.AppTheme
    }
  }
}