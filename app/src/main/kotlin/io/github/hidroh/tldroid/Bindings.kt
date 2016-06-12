package io.github.hidroh.tldroid

import android.content.Context
import android.database.Cursor
import android.databinding.BaseObservable
import android.databinding.BindingAdapter
import android.support.annotation.AttrRes
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.webkit.WebView
import android.widget.TextView

object Bindings {
  private val FORMAT_HTML_COLOR = "%06X"

  @JvmStatic
  @BindingAdapter("bind:monospace")
  fun setFont(textView: TextView, enabled: Boolean) {
    if (enabled) {
      textView.typeface = Application.MONOSPACE_TYPEFACE
    }
  }

  @JvmStatic
  @BindingAdapter("bind:highlightText", "bind:highlightColor")
  fun highlightText(textView: TextView, highlightText: String,
                    @AttrRes highlightColor: Int) {
    if (TextUtils.isEmpty(highlightText)) {
      return
    }
    val spannable = SpannableString(textView.text)
    val start = TextUtils.indexOf(spannable, highlightText)
    if (start >= 0) {
      spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(
          textView.context, getIdRes(textView.context, highlightColor))),
          start, start + highlightText.length,
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    textView.text = spannable
  }

  @JvmStatic
  @BindingAdapter("bind:html",
      "bind:htmlBackgroundColor",
      "bind:htmlTextColor",
      "bind:htmlLinkColor",
      "bind:htmlTextSize",
      "bind:htmlMargin")
  fun setHtml(webView: WebView,
              html: String?,
              @AttrRes backgroundColor: Int,
              @AttrRes textColor: Int,
              @AttrRes linkColor: Int,
              textSize: Float,
              margin: Float) {
    if (TextUtils.isEmpty(html)) {
      return
    }
    webView.setBackgroundColor(ContextCompat.getColor(webView.context,
        getIdRes(webView.context, backgroundColor)))
    webView.loadDataWithBaseURL(null,
        wrapHtml(webView.context, html, textColor, linkColor, textSize, margin),
        "text/html", "UTF-8", null)
  }

  private fun wrapHtml(context: Context, html: String?,
                                     @AttrRes textColor: Int,
                                     @AttrRes linkColor: Int,
                                     textSize: Float,
                                     margin: Float): String {
    return context.getString(R.string.styled_html,
        html,
        toHtmlColor(context, textColor),
        toHtmlColor(context, linkColor),
        toHtmlPx(context, textSize),
        toHtmlPx(context, margin),
        toHtmlColor(context, R.attr.colorKeywords),
        toHtmlColor(context, R.attr.colorLiterals))
  }

  private fun toHtmlColor(context: Context, @AttrRes colorAttr: Int): String {
    return String.format(FORMAT_HTML_COLOR, 0xFFFFFF and ContextCompat.getColor(context,
        getIdRes(context, colorAttr)))
  }

  private fun toHtmlPx(context: Context, dimen: Float): Float {
    return dimen / context.resources.displayMetrics.density
  }

  @IdRes
  private fun getIdRes(context: Context, @AttrRes attrRes: Int): Int {
    val ta = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
    val resId = ta.getResourceId(0, 0)
    ta.recycle()
    return resId
  }

  class Command : BaseObservable() {
    var name: String? = null
    var platform: String? = null

    companion object {

      @JvmStatic
      fun fromProvider(cursor: Cursor): Command {
        val command = Command()
        command.name = cursor.getString(cursor.getColumnIndexOrThrow(
            TldrProvider.CommandEntry.COLUMN_NAME))
        command.platform = cursor.getString(cursor.getColumnIndexOrThrow(
            TldrProvider.CommandEntry.COLUMN_PLATFORM))
        return command
      }
    }
  }
}
