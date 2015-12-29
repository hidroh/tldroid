package io.github.hidroh.tldroid;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_main);
        findViewById(R.id.info_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo();
            }
        });
        mEditText = (AutoCompleteTextView) findViewById(R.id.edit_text);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_SEARCH &&
                        search(v.getText().toString(), null);
            }
        });
        mEditText.setAdapter(new CursorAdapter(this));
        mEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CharSequence text = ((TextView) view.findViewById(android.R.id.text1)).getText();
                CharSequence platform = ((TextView) view.findViewById(android.R.id.text2)).getText();
                mEditText.setText(text);
                mEditText.setSelection(text.length());
                search(text, platform);
            }
        });
    }

    private boolean search(CharSequence query, CharSequence platform) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }
        startActivity(new Intent(this, CommandActivity.class)
                .putExtra(CommandActivity.EXTRA_QUERY, query)
                .putExtra(CommandActivity.EXTRA_PLATFORM, platform));
        return true;
    }

    private void showInfo() {
        WebView webView = new WebView(this);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setBackgroundColor(ContextCompat.getColor(this,
                Utils.getIdRes(this, android.R.attr.colorBackground)));
        webView.loadDataWithBaseURL(null, Utils.wrapHtml(this, getString(R.string.about_html)),
                "text/html", "UTF-8", null);
        new AlertDialog.Builder(this)
                .setView(webView)
                .create()
                .show();
    }

    private static class CursorAdapter extends ResourceCursorAdapter {

        private final LayoutInflater mInflater;

        public CursorAdapter(final Context context) {
            super(context, R.layout.dropdown_item, null, false);
            mInflater = LayoutInflater.from(context);
            setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    String queryString = constraint != null ? constraint.toString() : "";
                    return context.getContentResolver()
                            .query(TldrProvider.URI_COMMAND,
                                    null,
                                    TldrProvider.CommandEntry.COLUMN_NAME + " LIKE ?",
                                    new String[]{"%" + queryString + "%"},
                                    null);
                }
            });
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return newDropDownView(context, cursor, parent);
        }

        @Override
        public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
            ViewDataBinding holder = DataBindingUtil.inflate(mInflater, R.layout.dropdown_item,
                    parent, false);
            View view = holder.getRoot();
            view.setTag(R.id.dataBinding, holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((ViewDataBinding) view.getTag(R.id.dataBinding))
                    .setVariable(io.github.hidroh.tldroid.BR.command,
                            Bindings.Command.fromProvider(cursor));
        }
    }
}
