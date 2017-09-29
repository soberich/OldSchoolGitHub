package soberich.oldstylegithub;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import java.util.List;

import javax.inject.Inject;

import static soberich.oldstylegithub.GitHubUsersContentProvider.*;

/**
 *
 * Created by soberich on 9/29/17.
 */

public final class Processor {
    private static final String TAG = Processor.class.getName();

    private Context mContext;
    // TODO Di
    @Inject
    public Processor(Context context) {
        mContext = context;
    }

    public void dispatchBulkUsers(List<UserEntity> users) {
        ContentValues[] contentValues = new ContentValues[users.size()];
        ContentResolver cr = mContext.getContentResolver();
        for (int i = 0, usersSize = users.size(); i < usersSize; i++) {
            ContentValues cv = new ContentValues();
            cv.put(ID_COLUMN_TITLE, users.get(i).getId());
            cv.put(LOGIN_COLUMN_TITLE, users.get(i).getLogin());
            cv.put(NAME_COLUMN_TITLE, users.get(i).getName());
            cv.put(AVATAR_URL_COLUMN_TITLE, users.get(i).getAvatarUrl());
            contentValues[i] = cv;
        }
        cr.bulkInsert(UsersContract.Users.USERS_URI, contentValues);
    }

    public void dispatchSingleUser(UserEntity user) {
        ContentValues cv = new ContentValues();
        ContentResolver cr = mContext.getContentResolver();
        cv.put(ID_COLUMN_TITLE, user.getId());
        cv.put(LOGIN_COLUMN_TITLE, user.getLogin());
        cv.put(NAME_COLUMN_TITLE, user.getName());
        cv.put(AVATAR_URL_COLUMN_TITLE, user.getAvatarUrl());

        cr.insert(UsersContract.Users.USER_LOGIN_URI_PATTERN, cv);
    }
}
