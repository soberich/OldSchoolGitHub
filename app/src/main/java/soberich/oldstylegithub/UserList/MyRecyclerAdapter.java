package soberich.oldstylegithub.UserList;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by soberich on 9/30/17.
 */

public class MyRecyclerAdapter<T> extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private static final String TAG = "MyRecyclerAdapter";

    private int mHolderLayoutId, BRid;
    public List<T> mItems = new ArrayList<>();
    private IOnItemClickListener<T> onItemClickListener;

    public MyRecyclerAdapter(int holderLayoutId, int BRid, List<T> items) {
        this.mHolderLayoutId = holderLayoutId;
        this.BRid = BRid;
        this.mItems = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(mHolderLayoutId, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final T item = mItems.get(position);
        final int pos = holder.getAdapterPosition();

        holder.getViewDataBinding().getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(view, pos, item);
                //else throw new IllegalStateException("OnClickListener is not set!");
            }
        });
        holder.getViewDataBinding().setVariable(BRid, item);
        holder.getViewDataBinding().executePendingBindings();
    }
    @Override
    public int getItemCount() {
        return mItems.size();
    }
    public void setOnItemClickListener(IOnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void addItems(List<T> items) {
        mItems.addAll(items);

    }

    public void swapItems(List<T> items) {

        mItems.clear();
        mItems.addAll(items);

    }

    public interface IOnItemClickListener<T> {
        void onItemClick(View view, int position, T item);
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding mBinding;
        ViewHolder(View v) {
            super(v);
            mBinding = DataBindingUtil.bind(v);
        }
        ViewDataBinding getViewDataBinding() {
            return mBinding;
        }
    }
}
