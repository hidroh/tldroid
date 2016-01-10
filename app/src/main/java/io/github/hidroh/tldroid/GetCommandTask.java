package io.github.hidroh.tldroid;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.github.rjeschke.txtmark.Processor;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.zip.ZipFile;

class GetCommandTask extends AsyncTask<String, Void, String> {
    public static final String ZIP_FILENAME = "tldr.zip";
    private static final String COMMAND_PATH = "pages/%1$s/%2$s.md";

    private final WeakReference<CommandActivity> mCommandActivity;
    private final String mPlatform;

    public GetCommandTask(CommandActivity commandActivity, String platform) {
        mCommandActivity = new WeakReference<>(commandActivity);
        mPlatform = platform;
    }

    @Override
    protected final String doInBackground(String... params) {
        if (mCommandActivity.get() == null) {
            return null;
        }
        long lastModified = PreferenceManager
                .getDefaultSharedPreferences(mCommandActivity.get())
                .getLong(SyncService.PREF_LAST_ZIPPED, 0L);
        String selection = TldrProvider.CommandEntry.COLUMN_NAME + "=? AND " +
                TldrProvider.CommandEntry.COLUMN_PLATFORM + "=? AND " +
                TldrProvider.CommandEntry.COLUMN_MODIFIED +">=?";
        String[] selectionArgs = new String[]{params[0], mPlatform, String.valueOf(lastModified)};
        Cursor cursor = mCommandActivity.get().getContentResolver()
                .query(TldrProvider.URI_COMMAND, null, selection, selectionArgs, null);
        String markdown;
        if (cursor != null && cursor.moveToFirst()) {
            markdown = cursor.getString(cursor.getColumnIndexOrThrow(
                    TldrProvider.CommandEntry.COLUMN_TEXT));
            cursor.close();
        } else {
            markdown = loadFromZip(params[0], mPlatform, lastModified);
        }
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
    }

    private String loadFromZip(String name, String platform, long lastModified) {
        String markdown;
        try {
            ZipFile zip = new ZipFile(new File(mCommandActivity.get().getCacheDir(), ZIP_FILENAME),
                    ZipFile.OPEN_READ);
            markdown = Utils.readUtf8(zip.getInputStream(zip.getEntry(
                    String.format(COMMAND_PATH, platform, name))));
            zip.close();
        } catch (IOException e) {
            return null;
        }
        persist(name, platform, markdown, lastModified);
        return markdown;
    }

    private void persist(String name, String platform, String markdown, long lastModified) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation
                .newUpdate(TldrProvider.URI_COMMAND)
                .withValue(TldrProvider.CommandEntry.COLUMN_TEXT, markdown)
                .withValue(TldrProvider.CommandEntry.COLUMN_MODIFIED, lastModified)
                .withSelection(TldrProvider.CommandEntry.COLUMN_PLATFORM + "=? AND " +
                                TldrProvider.CommandEntry.COLUMN_NAME + "=?",
                        new String[]{platform, name})
                .build());
        try {
            mCommandActivity.get().getContentResolver()
                    .applyBatch(TldrProvider.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            // no op
        }
    }
}
