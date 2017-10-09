package soberich.oldstylegithub;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

import javax.inject.Inject;

import static soberich.oldstylegithub.UsersContract.AUTHORITY;
import static soberich.oldstylegithub.UsersContract.Users.CONTENT_ITEM_TYPE;
import static soberich.oldstylegithub.UsersContract.Users.CONTENT_TYPE;
import static soberich.oldstylegithub.UsersContract.Users.DEFAULT_LIMIT;
import static soberich.oldstylegithub.UsersContract.Users.DEFAULT_SORT_ORDER;
import static soberich.oldstylegithub.UsersContract.Users.PATH_USERS;
import static soberich.oldstylegithub.UsersContract.Users.PATH_USER_LOGIN;
import static soberich.oldstylegithub.UsersContract.Users.USERS_PATH_POSITION;
import static soberich.oldstylegithub.UsersContract.Users.USERS_URI;
import static soberich.oldstylegithub.UsersContract.Users.USER_LOGIN_PATH_POSITION;
import static soberich.oldstylegithub.UsersContract.Users.USER_LOGIN_URI_BASE;

/**
 *
 * Created by soberich on 9/27/17.
 */

public class GitHubUsersContentProvider extends ContentProvider {

    private static final String TAG = "WholeApp";

    public static final String ID_COLUMN_TITLE = "id";
    public static final String LOGIN_COLUMN_TITLE = "login";
    public static final String NAME_COLUMN_TITLE = "name";
    public static final String AVATAR_URL_COLUMN_TITLE = "avatar_url";
    public static final String TIMESTAMP_COLUMN_TITLE = "created_at";
    public static final String[] PROJECTION;

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "githubusers.db";
    private static final String TABLE_NAME = "users";
    private static final String DB_CREATE_TABLE;

    private static final UriMatcher sUriMatcher;
    private DBHelper mDBHelper;
    //@Inject public ServiceHelper mServiceHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_USERS, USERS_PATH_POSITION);
        sUriMatcher.addURI(AUTHORITY, PATH_USER_LOGIN + "#", USER_LOGIN_PATH_POSITION);

        PROJECTION = new String[] {
                BaseColumns._ID,
                ID_COLUMN_TITLE,
                LOGIN_COLUMN_TITLE,
                NAME_COLUMN_TITLE,
                AVATAR_URL_COLUMN_TITLE,
                TIMESTAMP_COLUMN_TITLE
        };

        DB_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + BaseColumns._ID +         " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + ID_COLUMN_TITLE +         " INTEGER UNIQUE, "
                        + LOGIN_COLUMN_TITLE +      " TEXT UNIQUE, "
                        + NAME_COLUMN_TITLE +       " TEXT, "
                        + AVATAR_URL_COLUMN_TITLE + " TEXT, "
                        + TIMESTAMP_COLUMN_TITLE +  " INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))"
                        + ");";
    }

    // BEGIN_INCLUDE (DNHelper)

    protected static final class DBHelper extends SQLiteOpenHelper {
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

    /*@Inject
    public GitHubUsersContentProvider(ServiceHelper sh) {
        super();
        this.mServiceHelper = sh;
    }*/

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        // TODO: 10/1/17 @Inject -tion SUCCESS!!! (1-Oct-2017)

        //((App)getContext()).getAppComponent().inject(this);
        //mServiceHelper = new ServiceHelper(getContext());
        return true;
    }

    @SuppressWarnings("ConstantConditions")
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
        Log.d(TAG, "getType() called with: uri = [" + uri + "]");
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

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues initialValues) {
        Log.d(TAG, "insert() called with: uri = [" + uri + "], initialValues = [" + initialValues + "]");
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
        long rowId = getDB().replace(TABLE_NAME,null, values);
        if (rowId > 0) {
            Uri userUri = ContentUris.withAppendedId(USER_LOGIN_URI_BASE, rowId);
            getContext().getContentResolver().notifyChange(userUri, null);
            return userUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int bulkInsert(@NonNull Uri uri,
                          @NonNull ContentValues[] initialValues) {
        Log.d(TAG, "ContentProvider.bulkInsert() called with: uri = [" + uri + "], initialValues = [" + Arrays.toString(initialValues) + "]");
        Log.d(TAG, "ContentProvider.bulkInsert() Thread is - " + Thread.currentThread().getName());
        if (sUriMatcher.match(uri) != USERS_PATH_POSITION) {
            throw new IllegalArgumentException("URI must be array (Cont. Values) of single Users" + uri);
        }

        int affected = 0;

        for (final ContentValues value : initialValues) {

            affected += getDB().replace(TABLE_NAME,null, value) > 0 ? 1 : 0;
        }
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        if (affected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return affected;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String where,
                      @Nullable String[] whereArgs) {

        // FIXME: 10/1/17 ONE or MORE rows!!
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

    @SuppressWarnings("ConstantConditions")
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String where,
                        @Nullable String[] whereArgs,
                        @Nullable String sortOrder) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Log.d("puff", "ContentProvider.query() called from "
        + "\n" + stackTraceElements[0]
        + "\n" + stackTraceElements[1]
        + "\n" + stackTraceElements[2]
        + "\n" + stackTraceElements[3]
        + "\n" + stackTraceElements[4]
        + "\n" + stackTraceElements[5]
        + "\n" + stackTraceElements[6]
        + "\n" + stackTraceElements[7]
        + "\n" + stackTraceElements[8]
        + "\n" + stackTraceElements[9]
        + "\n" + stackTraceElements[10]
        + "\n" + stackTraceElements[11]
        + "\n" + stackTraceElements[12]
        + "\n" + stackTraceElements[13]
        + "\n" + stackTraceElements[14]
        + "\n" + stackTraceElements[15]
        + "\n" + stackTraceElements[16]
        );
        Log.d(TAG, "ContentProvider.query() called with: uri = [" + uri + "], projection = [" + projection + "], where = [" + where + "], whereArgs = [" + whereArgs[0] + "], sortOrder = [" + sortOrder + "]");
        Log.d(TAG, "ContentProvider.query() Thread is - " + Thread.currentThread().getName());
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
                Log.d(TAG, String.valueOf("USERS_PATH_POSITION"));

                c = getDB().query(TABLE_NAME,
                        projection,
                        where,
                        whereArgs,
                        null,
                        null,
                        orderBy,
                        DEFAULT_LIMIT);

                // FIXME: 10/3/17 The weakest part of app - crooked nail (костыль!)
                // TODO: 10/3/17 Как достать since из whereArgs оптимально???
                // TODO: 10/3/17 Нужен еще кейс с PATH_POSITION 3 для getALLUsers и всё )
                int ifSinceProvidedThenIts = 0;
                if (where.contains(ID_COLUMN_TITLE)) {
                    String[] s = TextUtils.split(where, ",");
                    for (int i = 0; i < s.length; i++) {
                        if (s[i].contains(ID_COLUMN_TITLE))
                            ifSinceProvidedThenIts = Integer.valueOf(whereArgs[i]);
                    }
                    Intent intent = new Intent();
                    intent.putExtra(ID_COLUMN_TITLE, ifSinceProvidedThenIts);
                    ServiceHelper.execute(getContext(), USERS_PATH_POSITION, intent);
                }

                c.setNotificationUri(getContext().getContentResolver(), USERS_URI);
                break;
            case USER_LOGIN_PATH_POSITION:
                Log.d(TAG, String.valueOf("USER_LOGIN_PATH_POSITION"));
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
                Uri userUri = ContentUris.withAppendedId(USER_LOGIN_URI_BASE, UserID);
                c.setNotificationUri(getContext().getContentResolver(), userUri);
                break;
            default:
                throw new IllegalArgumentException("unsupported uri: " + uri);
        }

        return c;
    }



    private SQLiteQueryBuilder buildQuery(Uri uri, int match) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        switch (match) {
            case USERS_PATH_POSITION:
                break;
            case USER_LOGIN_PATH_POSITION:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        return builder;
    }

}



