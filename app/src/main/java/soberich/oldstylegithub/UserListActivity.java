package soberich.oldstylegithub;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import soberich.oldstylegithub.UserList.MyRecyclerAdapter;
import soberich.oldstylegithub.databinding.ActivityUserListBinding;

import static soberich.oldstylegithub.GitHubUsersContentProvider.AVATAR_URL_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.ID_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.LOGIN_COLUMN_TITLE;
import static soberich.oldstylegithub.GitHubUsersContentProvider.NAME_COLUMN_TITLE;

/**
 * An activity representing a list of Users. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link UserDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class UserListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    //private static final String TAG = "UserListActivity";
    private static final String TAG = "WholeApp";
    public static final String SINCE = "SINCE";
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private MyRecyclerAdapter mAdapter;
    private ActivityUserListBinding mBinding;
    private List<UserEntity> list = new ArrayList<>();
    private MyRecyclerAdapter.IOnItemClickListener onListItemClickListener;
    private RecyclerView mRecyclerView;

    private int previousTotal;
    private boolean loading = true;
    private final int visibleThreshold = 5;
    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private int since = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding
                = DataBindingUtil.setContentView(this, R.layout.activity_user_list);
        setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setTitle(getTitle());
        // TODO Undo listener
        mBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", null).show();
            }
        });

        if (findViewById(R.id.user_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
        //Log.d("fuck", onListItemClickListener.toString() + " " + String.valueOf(onListItemClickListener == null));
        assert onListItemClickListener != null;
        initOnItemClickListener();

        //Log.d("fuck", onListItemClickListener.toString() + " " + String.valueOf(onListItemClickListener == null));

        mRecyclerView = findViewById(R.id.user_list);
        assert mRecyclerView != null;
        setupRecyclerView(mRecyclerView, onListItemClickListener);
        setupRecyclerViewScrollListener(mRecyclerView);


        // создаем лоадер для чтения данных
        Bundle bundle = new Bundle(1);
        bundle.putString(SINCE, String.valueOf(since));
        getSupportLoaderManager().initLoader(0, bundle, this);
//        if(getSupportLoaderManager().getLoader(0) == null) {
//            getSupportLoaderManager().initLoader(0, null, this);
//        } else {
//            getSupportLoaderManager().restartLoader(0, null, this);
//        }

    }

    private void setupRecyclerViewScrollListener(RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = recyclerView.getChildCount();
                //Log.d(TAG, "visibleItemCount: " + visibleItemCount);
                totalItemCount = recyclerView.getLayoutManager() .getItemCount();
                //Log.d(TAG, "totalItemCount: " + totalItemCount);
                firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                //Log.d(TAG, "firstVisibleItem: " + firstVisibleItem);
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    Bundle bundle = new Bundle(1);
                    bundle.putString(SINCE, String.valueOf(since));
                    getSupportLoaderManager().restartLoader(0, bundle, UserListActivity.this);

                    //viewModel.loadUsers(viewModel.getAllUsers().get(viewModel.getAllUsers().size() - 1).getId());

                    loading = true;
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initOnItemClickListener() {
        //if (onListItemClickListener == null) {
            onListItemClickListener = new MyRecyclerAdapter.IOnItemClickListener<UserEntity>() {
                @Override
                public void onItemClick(View view, int position, UserEntity user) {
                    Log.d(TAG, "onItemClick() called with: view = [" + view + "], position = [" + position + "], user = [" + user + "]");
                    String identifier = user.getLogin();
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(UserDetailFragment.ARG_ITEM_ID, identifier);
                        UserDetailFragment fragment = new UserDetailFragment();
                        fragment.setArguments(arguments);
                        UserListActivity.this.getSupportFragmentManager().beginTransaction()
                                .replace(R.id.user_detail_container, fragment)
                                .commit();
                    } else {
                        Log.d(TAG, "onItemClick() called with: view: "+user.getLogin());
                        Context context = view.getContext();
                        Intent intent = new Intent(context, UserDetailActivity.class);
                        intent.putExtra(UserDetailFragment.ARG_ITEM_ID, identifier);
                        view.setTransitionName(identifier);
                        ActivityOptionsCompat activityOptions
                                = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                UserListActivity.this,
                                // Now we provide a list of Pair items which contain the view we can transitioning
                                // from, and the name of the view it is transitioning to, in the launched activity
                                Pair.create(view, identifier)
                        );

                        context.startActivity(intent, activityOptions.toBundle());
                        // END_INCLUDE(on_click)
                    }
                }
            };
        //}
    }

    @SuppressWarnings("unchecked")
    private void setupRecyclerView(@NonNull RecyclerView recyclerView,
                                   @NonNull MyRecyclerAdapter.IOnItemClickListener onClickListener) {
        mAdapter = new MyRecyclerAdapter<>(R.layout.user_list_content, BR.user, list);
        mAdapter.setOnItemClickListener(onClickListener);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader() called with: i = [" + i + "], bundle = [" + bundle + "]");
        Log.d(TAG, "onCreateLoader() Thread is - " + Thread.currentThread().getName());
        return new CursorLoader(this,
                UsersContract.Users.USERS_URI,
                GitHubUsersContentProvider.PROJECTION,
                "id > ?",
                new String[]{ bundle.getString(SINCE) }, // TODO switch to Bundle Int to handle killing process and config change
                null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        Log.d(TAG, "onLoadFinished() called with: loader = [" + loader + "], c = [" + c + "]");
        Log.d(TAG, "onLoadFinished() Thread is - " + Thread.currentThread().getName());
        ArrayList<UserEntity> list = new ArrayList<>(30);
        int count = 1;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            UserEntity user = new UserEntity();
            user.setAvatarUrl(c.getString(c.getColumnIndex(AVATAR_URL_COLUMN_TITLE)));
            user.setLogin(c.getString(c.getColumnIndex(LOGIN_COLUMN_TITLE)));
            user.setName(c.getString(c.getColumnIndex(NAME_COLUMN_TITLE)));
            user.setId(c.getInt(c.getColumnIndex(ID_COLUMN_TITLE)));
            Log.d(TAG, "Cursor contains USER: " + String.valueOf(count++) + " " + user.getLogin() + user.getId());
            list.add(user);
        }
        mAdapter.addItems(list);
        mAdapter.notifyDataSetChanged();
        Log.d("fuck", ""+since);
        since = list.size() != 0 ? list.get(list.size()-1).getId() : 0;
        Log.d("fuck", ""+since);
        //getSupportLoaderManager().destroyLoader(0);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset() called with: loader = [" + loader + "]");
        Log.d(TAG, "onLoaderReset() Thread is - " + Thread.currentThread().getName());
        //TODO need more convenient solution here (suppose the cursor may not have been closed, regardless the Cont.Provider should close it upon its killing)
        //mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
    }
}
