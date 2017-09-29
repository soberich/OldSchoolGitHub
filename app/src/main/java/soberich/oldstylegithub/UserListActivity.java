package soberich.oldstylegithub;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import soberich.oldstylegithub.databinding.UserListContentBinding;

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

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private MyRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        // TODO Undo listener
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
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

        View recyclerView = findViewById(R.id.user_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);


        // создаем лоадер для чтения данных
        getSupportLoaderManager().initLoader(0, null, (android.support.v4.app.LoaderManager.LoaderCallbacks<Object>) this);

    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        mAdapter = new MyRecyclerViewAdapter(this, null, mTwoPane);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(this,
                UsersContract.Users.USERS_URI,
                GitHubUsersContentProvider.PROJECTION,
                "id > ?",
                new String[]{"0"}, // TODO switch to Bundle Int tp handle killing process and config change
                UsersContract.Users.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
        mAdapter.swapCursor(c);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public static class MyRecyclerViewAdapter
            extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {


        private UserListContentBinding mBinding;

        private final UserListActivity mParentActivity;
        private final List<UserEntity> mValues;
        private final Cursor mCursor;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserEntity item = (UserEntity) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(UserDetailFragment.ARG_ITEM_ID, item.getLogin());
                    UserDetailFragment fragment = new UserDetailFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.user_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra(UserDetailFragment.ARG_ITEM_ID, item.getLogin());

                    context.startActivity(intent);
                }
            }
        };

        //TODO @Inject
        MyRecyclerViewAdapter(UserListActivity parent,
                              Cursor users,
                              boolean twoPane) {
            mCursor = users;
            mParentActivity = parent;
            mTwoPane = twoPane;
            mValues = new ArrayList<>(users.getColumnCount() == 0 ? 1 : users.getColumnCount());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            mBinding = DataBindingUtil
                    .inflate(LayoutInflater.from(parent.getContext())
                            , R.layout.user_list_content
                            , parent
                            , false);

            return new ViewHolder(mBinding);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mBinding.setUser(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }


        public Cursor swapCursor(Cursor c) {

            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                UserEntity user = new UserEntity();
                user.setId(c.getInt(c.getColumnIndex(ID_COLUMN_TITLE)));
                user.setLogin(c.getString(c.getColumnIndex(AVATAR_URL_COLUMN_TITLE)));
                user.setName(c.getString(c.getColumnIndex(NAME_COLUMN_TITLE)));
                user.setAvatarUrl(c.getString(c.getColumnIndex(AVATAR_URL_COLUMN_TITLE)));
                mValues.add(user);
            }

            return new SimpleCursorAdapter(mParentActivity.getApplicationContext(),
                    R.layout.user_list_content,
                    mCursor,
                    new String[]{AVATAR_URL_COLUMN_TITLE, AVATAR_URL_COLUMN_TITLE },
                    new int[]{R.id.avatar_in_list, R.id.login},
                    0).swapCursor(c);
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final UserListContentBinding mBinding;

            ViewHolder(UserListContentBinding binding) {
                super(binding.getRoot());
                this.mBinding = binding;
            }
        }
    }
}
