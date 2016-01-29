package io.github.hidroh.tldroid;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class RunActivity extends AppCompatActivity {
    public static final String EXTRA_COMMAND = RunActivity.class.getName() + ".EXTRA_COMMAND";
    private static final String STATE_ERROR = "state:error";
    private static final String STATE_OUTPUT = "state:output";
    private TextView mOutput;
    private TextView mError;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_run);
        mOutput = (TextView) findViewById(R.id.output);
        mError = (TextView) findViewById(R.id.error);
        final String command = getIntent().getStringExtra(EXTRA_COMMAND);
        ((TextView) findViewById(R.id.prompt)).append(command);
        ((EditText) findViewById(R.id.edit_text))
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_GO) {
                            execute(command, v.getText().toString().trim());
                            return true;
                        }
                        return false;
                    }
                });
        if (savedInstanceState != null) {
            display(Pair.create(savedInstanceState.getString(STATE_OUTPUT),
                    savedInstanceState.getString(STATE_ERROR)));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_ERROR, mError.getText().toString());
        outState.putString(STATE_OUTPUT, mOutput.getText().toString());
    }

    private void execute(String command, String arguments) {
        if (TextUtils.isEmpty(arguments)) {
            new RunTask(this).execute(command);
        } else {
            new RunTask(this).execute(TextUtils.split(command + " " + arguments, "\\s+"));
        }
    }

    private void display(Pair<String, String> output) {
        if (!TextUtils.isEmpty(output.second)) {
            mError.setText(output.second);
            mError.setVisibility(View.VISIBLE);
            mOutput.setVisibility(View.GONE);
        } else {
            mOutput.setText(output.first);
            mOutput.setVisibility(View.VISIBLE);
            mError.setVisibility(View.GONE);
        }
    }

    private static class RunTask extends AsyncTask<String, Void, Pair<String, String>> {
        private final WeakReference<RunActivity> mRunActivity;

        public RunTask(RunActivity runActivity) {
            mRunActivity = new WeakReference<>(runActivity);
        }

        @Override
        protected Pair<String, String> doInBackground(String... params) {
            try {
                Process process = Runtime.getRuntime().exec(params);
                String stderr = Utils.readUtf8(process.getErrorStream());
                String stdout = Utils.readUtf8(process.getInputStream());
                process.destroy();
                return Pair.create(stdout, stderr);
            } catch (IOException e) {
                return Pair.create(null, e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Pair<String, String> output) {
            if (mRunActivity.get() != null) {
                mRunActivity.get().display(output);
            }
        }
    }
}
