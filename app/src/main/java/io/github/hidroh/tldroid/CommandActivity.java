package io.github.hidroh.tldroid;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CommandActivity extends AppCompatActivity {
    public static final String EXTRA_QUERY = CommandActivity.class.getName() + ".EXTRA_QUERY";
    public static final String EXTRA_PLATFORM = CommandActivity.class.getName() + ".EXTRA_PLATFORM";
    private static final String STATE_CONTENT = "state:content";
    private static final String PLATFORM_OSX = "osx";
    private String mContent;
    private String mQuery;
    private String mPlatform;
    private ViewDataBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQuery = getIntent().getStringExtra(EXTRA_QUERY);
        mPlatform = getIntent().getStringExtra(EXTRA_PLATFORM);
        setTitle(mQuery);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_command);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME |
                ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbar.setExpandedTitleTypeface(Application.MONOSPACE_TYPEFACE);
        collapsingToolbar.setCollapsedTitleTypeface(Application.MONOSPACE_TYPEFACE);
        WebView webView = (WebView) findViewById(R.id.web_view);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
        });
        if (savedInstanceState != null) {
            mContent = savedInstanceState.getString(STATE_CONTENT);
        }
        if (mContent == null && !TextUtils.isEmpty(mPlatform)) {
            new GetCommandTask(this, mPlatform).execute(mQuery);
        } else {
            render(mContent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_run, menu);
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
        menu.findItem(R.id.menu_run).setVisible(visible &&
                !PLATFORM_OSX.equalsIgnoreCase(mPlatform));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_run) {
            startActivity(new Intent(this, RunActivity.class)
                .putExtra(RunActivity.EXTRA_COMMAND, mQuery));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_CONTENT, mContent);
    }

    void render(String html) {
        mContent = html == null ? "" : html;
        supportInvalidateOptionsMenu();
        if (TextUtils.isEmpty(html)) {
            // just display a generic message for all scenarios for now
            html = getString(R.string.empty_html);
        }
        mBinding.setVariable(io.github.hidroh.tldroid.BR.content, html);
    }

}
