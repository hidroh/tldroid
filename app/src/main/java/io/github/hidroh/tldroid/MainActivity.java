package io.github.hidroh.tldroid;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_main);
        ((EditText) findViewById(R.id.edit_text))
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        return actionId == EditorInfo.IME_ACTION_SEARCH &&
                                search(v.getText().toString());
                    }
                });
    }

    private boolean search(String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }
        startActivity(new Intent(this, CommandActivity.class)
                .putExtra(CommandActivity.EXTRA_QUERY, query));
        return true;
    }

}
