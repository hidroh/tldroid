package io.github.hidroh.tldroid;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class SyncService extends IntentService {
    private static final String TAG = SyncService.class.getSimpleName();
    private static final String INDEX_URL = "http://tldr-pages.github.io/assets/index.json";
    private static final String ZIP_URL = "http://tldr-pages.github.io/assets/tldr.zip";
    public static final String EXTRA_ASSET_TYPE = TAG + ".EXTRA_ASSET_TYPE";
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
        Commands commands;
        try {
            InputStream response = new URL(INDEX_URL).openConnection().getInputStream();
            String responseString = Utils.readUtf8(response);
            commands = new GsonBuilder().create().fromJson(responseString, Commands.class);
        } catch (IOException | JsonSyntaxException e) {
            return;
        }
        if (commands.commands == null || commands.commands.length == 0) {
            return;
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (Command command : commands.commands) {
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

    private void syncZip() {
        try {
            Source response = Okio.source(new URL(ZIP_URL)
                    .openConnection()
                    .getInputStream());
            File file = new File(getCacheDir(), GetCommandTask.ZIP_FILENAME);
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            sink.writeAll(response);
            sink.close();
        } catch (IOException e) {
            // no op
        }
    }

    private static class Commands {
        Command[] commands;
    }

    private static class Command {
        String name;
        String[] platform;
    }
}
