package io.github.hidroh.tldroid;

import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.widget.TextView;

public class Bindings {

    @BindingAdapter({"bind:monospace"})
    public static void setFont(TextView textView, boolean enabled) {
        if (enabled) {
            textView.setTypeface(Application.MONOSPACE_TYPEFACE);
        }
    }

    public static class Command extends BaseObservable {
        private String name;
        private String platform;

        public static Command fromProvider(Cursor cursor) {
            Command command = new Command();
            command.name = cursor.getString(cursor.getColumnIndexOrThrow(
                    TldrProvider.CommandEntry.COLUMN_NAME));
            command.platform = cursor.getString(cursor.getColumnIndexOrThrow(
                    TldrProvider.CommandEntry.COLUMN_PLATFORM));
            return command;
        }

        @Bindable
        public String getName() {
            return name;
        }

        @Bindable
        public void setName(String name) {
            this.name = name;
        }

        @Bindable
        public String getPlatform() {
            return platform;
        }

        @Bindable
        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }
}
