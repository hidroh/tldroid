package io.github.hidroh.tldroid;

import android.databinding.BindingAdapter;
import android.widget.TextView;

public class Bindings {

    @BindingAdapter({"bind:monospace"})
    public static void setFont(TextView textView, boolean enabled) {
        if (enabled) {
            textView.setTypeface(Application.MONOSPACE_TYPEFACE);
        }
    }
}
