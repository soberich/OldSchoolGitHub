package soberich.oldstylegithub;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 *
 * Created by soberich on 9/27/17.
 */

public final class UsersContract {
    public static final String AUTHORITY = "soberich.oldstylegithub";

    private UsersContract() {}

    public static final class Users implements BaseColumns {

        public static final String SCHEME = "content://";
        public static final String TABLE_NAME = "users";
        public static final String PATH_USERS = "/users";
        public static final String PATH_USER_LOGIN = "/users/";
        public static final int USERS_PATH_POSITION = 1;
        public static final int USER_LOGIN_PATH_POSITION = 2;
        public static final Uri USERS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_USERS);
        public static final Uri USER_LOGIN_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_USER_LOGIN);
        public static final Uri USER_LOGIN_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_USER_LOGIN + "/#");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TABLE_NAME;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + TABLE_NAME;
        public static final String DEFAULT_SORT_ORDER = "id ASC";
        public static final String DEFAULT_LIMIT = "30";
        private Users() {}
    }
}