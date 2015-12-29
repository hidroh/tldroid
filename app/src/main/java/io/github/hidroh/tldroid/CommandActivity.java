package io.github.hidroh.tldroid;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class CommandActivity extends AppCompatActivity {
    public static final String EXTRA_QUERY = CommandActivity.class.getName() + ".EXTRA_QUERY";

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
