package io.github.hidroh.tldroid;

import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.github.rjeschke.txtmark.Processor;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.zip.ZipFile;

import okio.BufferedSource;
import okio.Okio;

class GetCommandTask extends AsyncTask<String, Void, String> {
    public static final String ZIP_FILENAME = "tldr.zip";
    private static final String COMMAND_PATH = "pages/%1$s/%2$s.md";

    private final WeakReference<CommandActivity> mCommandActivity;
    private final String mPlatform;
    private ZipFile mZipFile;

    public GetCommandTask(CommandActivity commandActivity, String platform) {
        mCommandActivity = new WeakReference<>(commandActivity);
        mPlatform = platform;
        try {
            mZipFile = new ZipFile(new File(commandActivity.getCacheDir(), ZIP_FILENAME),
                    ZipFile.OPEN_READ);
        } catch (IOException e) {
            mZipFile = null;
        }
    }

    @Override
    protected final String doInBackground(String... params) {
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
        String markdown = getMarkdown(params[0], platform);
        if (TextUtils.isEmpty(markdown)) {
            return null;
        }
        return Processor.process(markdown);
    }

    @Override
    protected void onPostExecute(String s) {
        if (mCommandActivity.get() != null) {
            mCommandActivity.get().render(s);
        }
        if (mZipFile != null) {
            try {
                mZipFile.close();
            } catch (IOException e) {
                // no op
            }
        }
    }

    private String getMarkdown(String name, String platform) {
        if (mZipFile == null) {
            return null;
        }
        try {
            BufferedSource source = Okio.buffer(Okio.source(
                    mZipFile.getInputStream(mZipFile.getEntry(
                            String.format(COMMAND_PATH, platform, name)))));
            String markdown = source.readUtf8();
            source.close();
            return markdown;
        } catch (IOException e) {
            return null;
        }
    }
}
