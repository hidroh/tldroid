package io.github.hidroh.tldroid

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.BaseColumns
import android.support.v4.widget.ResourceCursorAdapter
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.FilterQueryProvider
import android.widget.TextView

class MainActivity : AppCompatActivity() {
  private var mEditText: AutoCompleteTextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_main)
    findViewById(R.id.info_button)!!.setOnClickListener { showInfo() }
    findViewById(R.id.list_button)!!.setOnClickListener { showList() }
    mEditText = findViewById(R.id.edit_text) as AutoCompleteTextView?
    mEditText!!.setOnEditorActionListener { v, actionId, event ->
      actionId == EditorInfo.IME_ACTION_SEARCH && search(v.text.toString(), null) }
    mEditText!!.setAdapter(CursorAdapter(this))
    mEditText!!.onItemClickListener = AdapterView.OnItemClickListener {
      parent, view, position, id ->
      val text = (view.findViewById(android.R.id.text1) as TextView).text
      val platform = (view.findViewById(android.R.id.text2) as TextView).text
      search(text.toString(), platform)
    }
  }

  private fun search(query: CharSequence, platform: CharSequence?): Boolean {
    if (TextUtils.isEmpty(query)) {
      return false
    }
    mEditText!!.setText(query)
    mEditText!!.setSelection(query.length)
    startActivity(Intent(this, CommandActivity::class.java)
        .putExtra(CommandActivity.EXTRA_QUERY, query)
        .putExtra(CommandActivity.EXTRA_PLATFORM, platform))
    return true
  }

  private fun showInfo() {
    val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater,
        R.layout.web_view, null, false)
    val lastRefreshed = PreferenceManager.getDefaultSharedPreferences(this)
        .getLong(SyncService.PREF_LAST_REFRESHED, 0L)
    val lastRefreshedText = if (lastRefreshed > 0)
      DateUtils.getRelativeDateTimeString(this, lastRefreshed,
          DateUtils.HOUR_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0)
    else
      getString(R.string.never)
    val totalCommands = PreferenceManager.getDefaultSharedPreferences(this)
        .getInt(SyncService.PREF_COMMAND_COUNT, 0)
    binding.setVariable(io.github.hidroh.tldroid.BR.content,
        getString(R.string.info_html, lastRefreshedText, totalCommands) +
            getString(R.string.about_html))
    binding.root.id = R.id.web_view
    AlertDialog.Builder(this).setView(binding.root).create().show()
  }

  private fun showList() {
    val adapter = CursorAdapter(this)
    adapter.filter.filter("")
    AlertDialog.Builder(this)
        .setTitle(R.string.all_commands)
        .setAdapter(adapter, DialogInterface.OnClickListener { dialog, which ->
          val cursor = adapter.getItem(which) as Cursor? ?: return@OnClickListener
          search(cursor.getString(cursor.getColumnIndex(TldrProvider.CommandEntry.COLUMN_NAME)),
              cursor.getString(cursor.getColumnIndex(TldrProvider.CommandEntry.COLUMN_PLATFORM)))
        })
        .create()
        .show()
  }

  private class CursorAdapter(context: Context) :
      ResourceCursorAdapter(context, R.layout.dropdown_item, null, false) {

    private val mInflater: LayoutInflater
    private var mQueryString: String? = null

    init {
      mInflater = LayoutInflater.from(context)
      filterQueryProvider = FilterQueryProvider { constraint ->
        mQueryString = if (constraint != null) constraint.toString() else ""
        context.contentResolver.query(TldrProvider.URI_COMMAND,
            arrayOf(BaseColumns._ID,
                TldrProvider.CommandEntry.COLUMN_NAME,
                TldrProvider.CommandEntry.COLUMN_PLATFORM),
            "${TldrProvider.CommandEntry.COLUMN_NAME} LIKE ?",
            arrayOf("%$mQueryString%"),
            null)
      }
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
      return newDropDownView(context, cursor, parent)
    }

    override fun newDropDownView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
      val holder = DataBindingUtil.inflate<ViewDataBinding>(mInflater, R.layout.dropdown_item,
          parent, false)
      val view = holder.root
      view.setTag(R.id.dataBinding, holder)
      return view
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
      val binding = view.getTag(R.id.dataBinding) as ViewDataBinding
      binding.setVariable(io.github.hidroh.tldroid.BR.command,
          Bindings.Command.fromProvider(cursor))
      binding.setVariable(io.github.hidroh.tldroid.BR.highlight, mQueryString)
    }

    override fun convertToString(cursor: Cursor?): CharSequence {
      return cursor!!.getString(cursor.getColumnIndexOrThrow(
          TldrProvider.CommandEntry.COLUMN_NAME))
    }
  }
}
