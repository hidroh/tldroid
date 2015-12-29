package io.github.hidroh.tldroid;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.google.gson.GsonBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;

public class SyncService extends IntentService {
    private static final String TAG = SyncService.class.getSimpleName();
    private static final String INDEX_URL = "https://raw.githubusercontent.com/tldr-pages/tldr/master/pages/index.json";
    private OkHttpClient mClient;

    public SyncService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClient = new OkHttpClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Response response;
        try {
            response = mClient.newCall(new Request.Builder()
                    .url(HttpUrl.parse(INDEX_URL))
                    .build())
                    .execute();
        } catch (IOException e) {
            return;
        }
        Commands commands;
        try {
            commands =new GsonBuilder().create().fromJson(response.body().string(), Commands.class);
        } catch (IOException e) {
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

    private static class Commands {
        Command[] commands;
    }

    private static class Command {
        String name;
        String[] platform;
    }
}
