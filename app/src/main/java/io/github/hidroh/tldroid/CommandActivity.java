package io.github.hidroh.tldroid;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
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
    public static final String EXTRA_PLATFORM = CommandActivity.class.getName() + ".EXTRA_PLATFORM";
    private static final String STATE_CONTENT = "state:content";
    private WebView mWebView;
    private View mProgressBar;
    private String mContent;
    private String mQuery;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getIntent().getStringExtra(EXTRA_QUERY);
        String platform = getIntent().getStringExtra(EXTRA_PLATFORM);
        setTitle(mQuery);
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
                Utils.getIdRes(this, android.R.attr.colorBackground)));
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        });
        if (savedInstanceState != null) {
            mContent = savedInstanceState.getString(STATE_CONTENT);
        }
        if (mContent == null) {
            new GetCommandTask(this, platform).execute(mQuery);
        } else {
            render(mContent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemShare = menu.findItem(R.id.menu_share);
        boolean visible = !TextUtils.isEmpty(mContent);
        itemShare.setVisible(visible);
        if (visible) {
            ((ShareActionProvider) MenuItemCompat.getActionProvider(itemShare))
                    .setShareIntent(new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, mQuery)
                            .putExtra(Intent.EXTRA_TEXT, Html.fromHtml(mContent).toString()));
        }
        return super.onPrepareOptionsMenu(menu);
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
        supportInvalidateOptionsMenu();
        if (TextUtils.isEmpty(html)) {
            // just display a generic message for all scenarios for now
            html = getString(R.string.empty_html);
        }
        mWebView.loadDataWithBaseURL(null, Utils.wrapHtml(this, html), "text/html", "UTF-8", null);
    }

    private static class GetCommandTask extends AsyncTask<String, Void, String> {
        private static final String BASE_URL = "https://raw.githubusercontent.com/tldr-pages/tldr/master/pages";
        private final WeakReference<CommandActivity> mCommandActivity;
        private final OkHttpClient mClient;
        private final String mPlatform;

        public GetCommandTask(CommandActivity commandActivity, String platform) {
            mCommandActivity = new WeakReference<>(commandActivity);
            mPlatform = platform;
            mClient = new OkHttpClient();
        }

        @Override
        protected String doInBackground(String... params) {
            if (mCommandActivity.get() == null) {
                return null;
            }
            String nameQuery = params[0];
            String selection;
            String[] selectionArgs;
            if (TextUtils.isEmpty(mPlatform)) {
                selection = TldrProvider.CommandEntry.COLUMN_NAME + "=?";
                selectionArgs = new String[]{nameQuery};
            } else {
                selection = TldrProvider.CommandEntry.COLUMN_NAME + "=? AND " +
                        TldrProvider.CommandEntry.COLUMN_PLATFORM + "=?";
                selectionArgs = new String[]{nameQuery, mPlatform};
            }
            Cursor cursor = mCommandActivity.get().getContentResolver()
                    .query(TldrProvider.URI_COMMAND, null, selection, selectionArgs, null);
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
