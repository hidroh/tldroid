package io.github.hidroh.tldroid;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TldrProvider extends ContentProvider {
    static final String AUTHORITY = "io.github.hidroh.tldroid.provider";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    static final Uri URI_COMMAND = BASE_URI.buildUpon()
            .appendPath(CommandEntry.TABLE_NAME)
            .build();

    interface CommandEntry extends BaseColumns {
        String TABLE_NAME = "command";
        String MIME_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
        String COLUMN_NAME = "name";
        String COLUMN_PLATFORM = "platform";
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        if (URI_COMMAND.equals(uri)) {
            return db.query(CommandEntry.TABLE_NAME,
                    projection, selection, selectionArgs, null, null, null);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (URI_COMMAND.equals(uri)) {
            return CommandEntry.MIME_TYPE;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (URI_COMMAND.equals(uri)) {
            long id = db.insert(CommandEntry.TABLE_NAME, null, values);
            return id == -1 ? null : ContentUris.withAppendedId(URI_COMMAND, id);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (URI_COMMAND.equals(uri)) {
            return db.delete(CommandEntry.TABLE_NAME, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (URI_COMMAND.equals(uri)) {
            return db.update(CommandEntry.TABLE_NAME, values, selection, selectionArgs);
        }
        return 0;
    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "tldr.db";
        private static final int DB_VERSION = 1;
        private static final String TEXT_TYPE = " TEXT";
        private static final String INTEGER_TYPE = " INTEGER";
        private static final String PRIMARY_KEY = " PRIMARY KEY";
        private static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_COMMAND_TABLE =
                "CREATE TABLE " + CommandEntry.TABLE_NAME + " (" +
                        CommandEntry._ID + INTEGER_TYPE +  PRIMARY_KEY + COMMA_SEP +
                        CommandEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                        CommandEntry.COLUMN_PLATFORM + TEXT_TYPE + COMMA_SEP +
                        " UNIQUE (" + CommandEntry.COLUMN_NAME + ", " +
                        CommandEntry.COLUMN_PLATFORM + ") ON CONFLICT REPLACE)";
        private static final String SQL_DROP_COMMAND_TABLE =
                "DROP TABLE IF EXISTS " + CommandEntry.TABLE_NAME;

        private DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_COMMAND_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DROP_COMMAND_TABLE);
            db.execSQL(SQL_CREATE_COMMAND_TABLE);
        }
    }
}
