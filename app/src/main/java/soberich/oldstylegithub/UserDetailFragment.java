package soberich.oldstylegithub;

import android.app.Activity;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import soberich.oldstylegithub.databinding.UserDetailBinding;

import static soberich.oldstylegithub.GitHubUsersContentProvider.AVATAR_URL_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.ID_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.LOGIN_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.NAME_COLUMN_TITLE;


/**
 * A fragment representing a single User detail screen.
 * This fragment is either contained in a {@link UserListActivity}
 * in two-pane mode (on tablets) or a {@link UserDetailActivity}
 * on handsets.
 */
public class UserDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "WholeApp";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "login";
    public static final String LOGIN = "LOGIN";

    /**
     * The dummy content this fragment is presenting.
     */
    private UserEntity mItem;
    private UserDetailBinding mBindng;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserDetailFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            Bundle bundle = new Bundle(1);
            bundle.putString(LOGIN, getArguments().getString(ARG_ITEM_ID));
            getActivity().getSupportLoaderManager().initLoader(1, bundle, this);


            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(getArguments().getString(ARG_ITEM_ID));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBindng = DataBindingUtil.inflate(inflater, R.layout.user_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            mBindng.setUser(mItem);
        }

        return mBindng.getRoot();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader() called with: i = [" + i + "], bundle = [" + bundle + "]");
        Log.d(TAG, "onCreateLoader() Thread is - " + Thread.currentThread().getName());
        return new CursorLoader(this.getContext(),
                UsersContract.Users.USER_LOGIN_URI_PATTERN,
                GitHubUsersContentProvider.PROJECTION,
                ARG_ITEM_ID + " = ?",
                new String[]{ bundle.getString(LOGIN) }, // TODO switch to Bundle Int to handle killing process and config change
                null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        Log.d(TAG, "onLoadFinished() called with: loader = [" + loader + "], c = [" + c + "]");
        Log.d(TAG, "onLoadFinished() Thread is - " + Thread.currentThread().getName());

        c.moveToFirst();
        UserEntity user = new UserEntity();
        user.setAvatarUrl(c.getString(c.getColumnIndex(AVATAR_URL_COLUMN_TITLE)));
        user.setLogin(c.getString(c.getColumnIndex(LOGIN_COLUMN_TITLE)));
        user.setName(c.getString(c.getColumnIndex(NAME_COLUMN_TITLE)));
        user.setId(c.getInt(c.getColumnIndex(ID_COLUMN_TITLE)));

        mBindng.setUser(user);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset() called with: loader = [" + loader + "]");
        Log.d(TAG, "onLoaderReset() Thread is - " + Thread.currentThread().getName());
        //TODO need more convenient solution here (suppose the cursor may not have been closed, regardless the Cont.Provider should close it upon its killing)
        //mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
    }
}
