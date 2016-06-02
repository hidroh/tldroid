package io.github.hidroh.tldroid

import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ShareActionProvider
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class CommandActivity : AppCompatActivity() {
  companion object {
    val EXTRA_QUERY = CommandActivity::class.java.name + ".EXTRA_QUERY"
    val EXTRA_PLATFORM = CommandActivity::class.java.name + ".EXTRA_PLATFORM"
    private val STATE_CONTENT = "state:content"
    private val PLATFORM_OSX = "osx"
  }

  private var mContent: String? = null
  private var mQuery: String? = null
  private var mPlatform: String? = null
  private var mBinding: ViewDataBinding? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mQuery = intent.getStringExtra(EXTRA_QUERY)
    mPlatform = intent.getStringExtra(EXTRA_PLATFORM)
    title = mQuery
    mBinding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_command)
    setSupportActionBar(findViewById(R.id.toolbar) as Toolbar?)
    supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_HOME or
        ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE
    val collapsingToolbar = findViewById(R.id.collapsing_toolbar_layout) as CollapsingToolbarLayout?
    collapsingToolbar!!.setExpandedTitleTypeface(Application.MONOSPACE_TYPEFACE)
    collapsingToolbar.setCollapsedTitleTypeface(Application.MONOSPACE_TYPEFACE)
    val webView = findViewById(R.id.web_view) as WebView?
    webView!!.setWebChromeClient(WebChromeClient())
    webView.setWebViewClient(object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        return true
      }
    })
    if (savedInstanceState != null) {
      mContent = savedInstanceState.getString(STATE_CONTENT)
    }
    if (mContent == null) {
      GetCommandTask(this, mPlatform).execute(mQuery)
    } else {
      render(mContent)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_run, menu)
    menuInflater.inflate(R.menu.menu_share, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val itemShare = menu.findItem(R.id.menu_share)
    val visible = !TextUtils.isEmpty(mContent)
    itemShare.isVisible = visible
    if (visible) {
      (MenuItemCompat.getActionProvider(itemShare) as ShareActionProvider)
          .setShareIntent(Intent(Intent.ACTION_SEND)
              .setType("text/plain")
              .putExtra(Intent.EXTRA_SUBJECT, mQuery)
              .putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mContent).toString()))
    }
    menu.findItem(R.id.menu_run).isVisible = visible && !TextUtils.equals(PLATFORM_OSX, mPlatform)
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }
    if (item.itemId == R.id.menu_run) {
      startActivity(Intent(this, RunActivity::class.java)
          .putExtra(RunActivity.EXTRA_COMMAND, mQuery))
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(STATE_CONTENT, mContent)
  }

  internal fun render(html: String?) {
    mContent = html ?: ""
    supportInvalidateOptionsMenu()
    // just display a generic message if empty for now
    mBinding!!.setVariable(io.github.hidroh.tldroid.BR.content,
        if (TextUtils.isEmpty(mContent)) getString(R.string.empty_html) else html)
  }
}
