package io.github.hidroh.tldroid;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okio.BufferedSink;
import okio.Okio;

public class SyncService extends IntentService {
    private static final String TAG = SyncService.class.getSimpleName();
    private static final String INDEX_URL = "http://tldr-pages.github.io/assets/index.json";
    private static final String ZIP_URL = "http://tldr-pages.github.io/assets/tldr.zip";
    public static final String EXTRA_ASSET_TYPE = TAG + ".EXTRA_ASSET_TYPE";
    public static final String PREF_LAST_REFRESHED = INDEX_URL;
    public static final String PREF_LAST_ZIPPED = ZIP_URL;
    public static final String PREF_COMMAND_COUNT = "PREF_COMMAND_COUNT";
    public static final int ASSET_TYPE_INDEX = 0;
    public static final int ASSET_TYPE_ZIP = 1;

    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getIntExtra(EXTRA_ASSET_TYPE, ASSET_TYPE_INDEX) == ASSET_TYPE_INDEX) {
            syncIndex();
        } else {
            syncZip();
        }
    }

    private void syncIndex() {
        HttpURLConnection connection;
        if ((connection = connect(INDEX_URL)) == null) {
            return;
        }
        try {
            persist(new GsonBuilder()
                    .create()
                    .fromJson(Utils.readUtf8(connection.getInputStream()),
                            Command[].class));
        } catch (IOException | JsonSyntaxException e) {
            Log.e(TAG, e.toString());
        } finally {
            connection.disconnect();
        }
    }

    private void syncZip() {
        HttpURLConnection connection;
        if ((connection = connect(ZIP_URL)) == null) {
            return;
        }
        try {
            BufferedSink sink = Okio.buffer(Okio.sink(new File(getCacheDir(),
                    GetCommandTask.ZIP_FILENAME)));
            sink.writeAll(Okio.source(connection.getInputStream()));
            sink.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection connect(String url) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        connection.setIfModifiedSince(sharedPrefs.getLong(url, 0L));
        try {
            connection.connect();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                connection.disconnect();
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
        sharedPrefs.edit()
                .putLong(url, connection.getLastModified())
                .commit();
        return connection;
    }

    private void persist(Command[] commands) {
        if (commands == null || commands.length == 0) {
            return;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putInt(PREF_COMMAND_COUNT, commands.length)
                .commit();
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (Command command : commands) {
            for (String platform : command.platform) {
                operations.add(ContentProviderOperation.newInsert(TldrProvider.URI_COMMAND)
                        .withValue(TldrProvider.CommandEntry.COLUMN_NAME, command.name)
                        .withValue(TldrProvider.CommandEntry.COLUMN_PLATFORM, platform)
                        .build());
            }
        }
        ContentResolver cr = getContentResolver();
        try {
            cr.applyBatch(TldrProvider.AUTHORITY, operations);
            cr.notifyChange(TldrProvider.URI_COMMAND, null);
        } catch (RemoteException | OperationApplicationException e) {
            // no op
        }
    }

    private static class Command {
        String name;
        String[] platform;
    }
}
