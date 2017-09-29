package soberich.oldstylegithub;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import static soberich.oldstylegithub.UsersContract.AUTHORITY;
import static soberich.oldstylegithub.UsersContract.Users.CONTENT_ITEM_TYPE;
import static soberich.oldstylegithub.UsersContract.Users.CONTENT_TYPE;
import static soberich.oldstylegithub.UsersContract.Users.DEFAULT_SORT_ORDER;
import static soberich.oldstylegithub.UsersContract.Users.PATH_USERS;
import static soberich.oldstylegithub.UsersContract.Users.PATH_USER_LOGIN;
import static soberich.oldstylegithub.UsersContract.Users.USERS_PATH_POSITION;
import static soberich.oldstylegithub.UsersContract.Users.USERS_URI;
import static soberich.oldstylegithub.UsersContract.Users.USER_LOGIN_PATH_POSITION;

/**
 *
 * Created by soberich on 9/27/17.
 */

public class GitHubUsersContentProvider extends ContentProvider {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "githubusers.db";
    private static final String TABLE_NAME = "users";
    public static final String ID_COLUMN_TITLE = "id";
    public static final String LOGIN_COLUMN_TITLE = "login";
    public static final String NAME_COLUMN_TITLE = "name";
    public static final String AVATAR_URL_COLUMN_TITLE = "avatar_url";
    public static final String TIMESTAMP_COLUMN_TITLE = "created_at";

    private static final String DB_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ID_COLUMN_TITLE + " INTEGER, "
                    + LOGIN_COLUMN_TITLE + " TEXT, "
                    + NAME_COLUMN_TITLE + " TEXT, "
                    + AVATAR_URL_COLUMN_TITLE + " TEXT, "
                    + TIMESTAMP_COLUMN_TITLE + " INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"
                    + ");";

    private DBHelper mDBHelper;

    public static final String[] PROJECTION = new String[] {
            BaseColumns._ID,
            ID_COLUMN_TITLE,
            LOGIN_COLUMN_TITLE,
            NAME_COLUMN_TITLE,
            AVATAR_URL_COLUMN_TITLE,
            TIMESTAMP_COLUMN_TITLE
    };

    private static UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_USERS, USERS_PATH_POSITION);
        sUriMatcher.addURI(AUTHORITY, PATH_USER_LOGIN + "#", USER_LOGIN_PATH_POSITION);
    }

    // BEGIN_INCLUDE (DNHelper)

    public static class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DB_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    // END_INCLUDE (DBHelper)

    private SQLiteDatabase getDB() {
        return mDBHelper.getWritableDatabase();
    }

    @Override
    public boolean onCreate() {
        // TODO: Implement this to initialize your content provider on startup.
        mDBHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String where,
                      @Nullable String[] whereArgs) {
        // Implement this to handle requests to delete one or more rows.
        int match = sUriMatcher.match(uri);
        int affected;

        switch (match) {
            case USERS_PATH_POSITION:
                affected = getDB().delete(TABLE_NAME,
                        (!TextUtils.isEmpty(where) ?
                                " AND (" + where + ')' : ""),
                        whereArgs);
                break;
            case USER_LOGIN_PATH_POSITION:
                long userId = ContentUris.parseId(uri);
                affected = getDB().delete(TABLE_NAME,
                        BaseColumns._ID + "=" + userId
                                + (!TextUtils.isEmpty(where) ?
                                " AND (" + where + ')' : ""),
                        whereArgs);
                break;
            default:
                throw new IllegalArgumentException("unknown video element: " +
                        uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        switch (sUriMatcher.match(uri)) {
            case USERS_PATH_POSITION:
                return CONTENT_TYPE;
            case USER_LOGIN_PATH_POSITION:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unkhown type: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues initialValues) {
        // TODO: Implement this to handle requests to insert a new row.
        if (sUriMatcher.match(uri) != USER_LOGIN_PATH_POSITION) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        long rowId = getDB().insert(TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri userUri = ContentUris.withAppendedId(USERS_URI, rowId);
            getContext().getContentResolver().notifyChange(userUri, null);
            return userUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri,
                          @NonNull ContentValues[] initialValues) {

        if (sUriMatcher.match(uri) != USERS_PATH_POSITION) {
            throw new IllegalArgumentException("URI must be array (Cont. Values) of single Users" + uri);
        }

        int affected = 0;

        for (final ContentValues value : initialValues) {

            affected += getDB().insert(TABLE_NAME,null, value) > 0 ? 1 : 0;
        }

        if (affected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return affected;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String where,
                        @Nullable String[] whereArgs,
                        @Nullable String sortOrder) {
        // TODO: Implement this to handle query requests from clients.
        String orderBy;
        if (TextUtils.isEmpty(sortOrder))
            orderBy = DEFAULT_SORT_ORDER;
        else
            orderBy = sortOrder;
        int match = sUriMatcher.match(uri);

        Cursor c;
        switch (match) {
            case USERS_PATH_POSITION:
                c = getDB().query(TABLE_NAME,
                        projection,
                        where,
                        whereArgs,
                        null,
                        null,
                        orderBy/*,
                        "30"*/);
                c.setNotificationUri(getContext().getContentResolver(), USERS_URI);
                break;
            case USER_LOGIN_PATH_POSITION:
                long UserID = ContentUris.parseId(uri);
                c = getDB().query(TABLE_NAME,
                        projection,
                        BaseColumns._ID + " = " + UserID +
                                (!TextUtils.isEmpty(where) ?
                                        " AND (" + where + ')' : ""),
                        whereArgs,
                        null,
                        null,
                        orderBy);
                c.setNotificationUri(getContext().getContentResolver(), USERS_URI);
                break;
            default:
                throw new IllegalArgumentException("unsupported uri: " + uri);
        }
        return c;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String where,
                      @Nullable String[] whereArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        int affected;

        switch (sUriMatcher.match(uri)) {
            case USERS_PATH_POSITION:
                affected = getDB().update(TABLE_NAME, values,
                        where, whereArgs);
                break;

            case USER_LOGIN_PATH_POSITION:
                String userId = uri.getPathSegments().get(1);
                affected = getDB().update(TABLE_NAME, values,
                        BaseColumns._ID + "=" + userId
                                + (!TextUtils.isEmpty(where) ?
                                " AND (" + where + ')' : ""),
                        whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return affected;
    }
}



