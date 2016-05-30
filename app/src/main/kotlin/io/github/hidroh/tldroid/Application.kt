package io.github.hidroh.tldroid

import android.content.Intent
import android.graphics.Typeface

class Application : android.app.Application() {
  companion object {
    var MONOSPACE_TYPEFACE: Typeface? = null
  }

  override fun onCreate() {
    super.onCreate()
    MONOSPACE_TYPEFACE = Typeface.createFromAsset(assets, "RobotoMono-Regular.ttf")
    startService(Intent(this, SyncService::class.java)
        .putExtra(SyncService.EXTRA_ASSET_TYPE, SyncService.ASSET_TYPE_INDEX))
    startService(Intent(this, SyncService::class.java)
        .putExtra(SyncService.EXTRA_ASSET_TYPE, SyncService.ASSET_TYPE_ZIP))
  }
}
