package io.github.hidroh.tldroid

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.util.Pair
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.io.IOException
import java.lang.ref.WeakReference

class RunActivity : ThemedActivity() {
  companion object {
    val EXTRA_COMMAND = RunActivity::class.java.name + ".EXTRA_COMMAND"
    private val STATE_ERROR = "state:error"
    private val STATE_OUTPUT = "state:output"
  }

  private var mOutput: TextView? = null
  private var mError: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_run)
    mOutput = findViewById(R.id.output) as TextView?
    mError = findViewById(R.id.error) as TextView?
    val command = intent.getStringExtra(EXTRA_COMMAND)
    (findViewById(R.id.prompt) as TextView).append(command)
    (findViewById(R.id.edit_text) as EditText).setOnEditorActionListener { v, actionId, event ->
      execute(command, v.text.toString().trim())
      true
    }
    if (savedInstanceState != null) {
      display(Pair.create(savedInstanceState.getString(STATE_OUTPUT),
          savedInstanceState.getString(STATE_ERROR)))
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(STATE_ERROR, mError!!.text.toString())
    outState.putString(STATE_OUTPUT, mOutput!!.text.toString())
  }

  private fun execute(command: String, arguments: String) {
    if (TextUtils.isEmpty(arguments)) {
      RunTask(this).execute(command)
    } else {
      RunTask(this).execute(*TextUtils.split("$command $arguments", "\\s+"))
    }
  }

  private fun display(output: Pair<String, String>) {
    if (!TextUtils.isEmpty(output.second)) {
      mError!!.text = output.second
      mError!!.visibility = View.VISIBLE
      mOutput!!.visibility = View.GONE
    } else {
      mOutput!!.text = output.first
      mOutput!!.visibility = View.VISIBLE
      mError!!.visibility = View.GONE
    }
  }

  internal class RunTask(runActivity: RunActivity) :
      AsyncTask<String, Void, Pair<String, String>>() {
    private val mRunActivity: WeakReference<RunActivity>

    init {
      mRunActivity = WeakReference(runActivity)
    }

    override fun doInBackground(vararg params: String): Pair<String, String> {
      try {
        val process = Runtime.getRuntime().exec(params)
        val stderr = Utils.readUtf8(process.errorStream)
        val stdout = Utils.readUtf8(process.inputStream)
        process.destroy()
        return Pair.create(stdout, stderr)
      } catch (e: IOException) {
        return Pair.create<String, String>(null, e.message)
      }

    }

    override fun onPostExecute(output: Pair<String, String>) {
      if (mRunActivity.get() != null) {
        mRunActivity.get().display(output)
      }
    }
  }
}
