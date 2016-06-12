package io.github.hidroh.tldroid

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup

open class ThemedActivity: AppCompatActivity() {
  override fun setContentView(layoutResID: Int) {
    setTheme(Utils.loadTheme(this))
    super.setContentView(layoutResID)
  }

  override fun setContentView(view: View?) {
    setTheme(Utils.loadTheme(this))
    super.setContentView(view)
  }

  override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
    setTheme(Utils.loadTheme(this))
    super.setContentView(view, params)
  }
}