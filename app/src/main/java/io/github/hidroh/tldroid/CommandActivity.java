package io.github.hidroh.tldroid;

import android.content.res.TypedArray;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.rjeschke.txtmark.Processor;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class CommandActivity extends AppCompatActivity {
    public static final String EXTRA_QUERY = CommandActivity.class.getName() + ".EXTRA_QUERY";
    private static final String STATE_CONTENT = "state:content";
    private static final String FORMAT_HTML_COLOR = "%06X";
    private WebView mWebView;
    private View mProgressBar;
    private String mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String query = getIntent().getStringExtra(EXTRA_QUERY);
        setTitle(query);
        DataBindingUtil.setContentView(this, R.layout.activity_command);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbar.setExpandedTitleTypeface(Application.MONOSPACE_TYPEFACE);
        collapsingToolbar.setCollapsedTitleTypeface(Application.MONOSPACE_TYPEFACE);
        mProgressBar = findViewById(R.id.progress);
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setBackgroundColor(ContextCompat.getColor(this,
                getIdRes(android.R.attr.colorBackground)));
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
            }
        });
        if (savedInstanceState != null) {
            mContent = savedInstanceState.getString(STATE_CONTENT);
        }
        if (mContent == null) {
            new GetCommandTask(this).execute(query);
        } else {
            render(mContent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CONTENT, mContent);
    }

    private void render(String html) {
        mContent = html == null ? "" : html;
        if (TextUtils.isEmpty(html)) {
            return; // TODO
        }
        mWebView.loadDataWithBaseURL(null, wrap(html), "text/html", "UTF-8", null);
    }

    private String wrap(String html) {
        return getString(R.string.command_html,
                html,
                toHtmlColor(android.R.attr.textColorPrimary),
                toHtmlPx(R.dimen.activity_horizontal_margin));
    }

    private String toHtmlColor(@AttrRes int colorAttr) {
        return String.format(FORMAT_HTML_COLOR, 0xFFFFFF &
                ContextCompat.getColor(this, getIdRes(colorAttr)));
    }

    private float toHtmlPx(@DimenRes int dimenAttr) {
        return getResources().getDimension(dimenAttr) / getResources().getDisplayMetrics().density;
    }

    private @IdRes int getIdRes(@AttrRes int attrRes) {
        TypedArray ta = getTheme().obtainStyledAttributes(new int[]{attrRes});
        int resId = ta.getResourceId(0, 0);
        ta.recycle();
        return resId;
    }

    private static class GetCommandTask extends AsyncTask<String, Void, String> {
        private static final String BASE_URL = "https://raw.githubusercontent.com/tldr-pages/tldr/master/pages";
        private final WeakReference<CommandActivity> mCommandActivity;
        private final OkHttpClient mClient;

        public GetCommandTask(CommandActivity commandActivity) {
            mCommandActivity = new WeakReference<>(commandActivity);
            mClient = new OkHttpClient();
        }

        @Override
        protected String doInBackground(String... params) {
            if (mCommandActivity.get() == null) {
                return null;
            }
            Cursor cursor = mCommandActivity.get().getContentResolver()
                    .query(TldrProvider.URI_COMMAND,
                            null,
                            TldrProvider.CommandEntry.COLUMN_NAME + "=?",
                            new String[]{params[0]},
                            null);
            if (cursor == null) {
                return null;
            }
            String platform = null;
            if (cursor.moveToFirst()) {
                platform = cursor.getString(cursor.getColumnIndexOrThrow(
                        TldrProvider.CommandEntry.COLUMN_PLATFORM));
            }
            cursor.close();
            if (TextUtils.isEmpty(platform)) {
                return null;
            }
            String markdown;
            try {
                markdown = mClient.newCall(new Request.Builder()
                        .url(HttpUrl.parse(BASE_URL)
                                .newBuilder()
                                .addPathSegment(platform)
                                .addPathSegment(params[0] + ".md")
                                .build())
                        .build())
                        .execute()
                        .body()
                        .string();
            } catch (IOException e) {
                return null;
            }
            return Processor.process(markdown);
        }

        @Override
        protected void onPostExecute(String s) {
            if (mCommandActivity.get() != null) {
                mCommandActivity.get().render(s);
            }
        }
    }
}
