package com.chemistrychatdrawing;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TestListItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTestRecyclerViewAdapter extends RecyclerView.Adapter<MyTestRecyclerViewAdapter.ViewHolder> {

    private final List<TestListItem.TItem> mValues;
    private MyTestRecyclerViewAdapter.OnItemClickLitener mOnItemClickLitener;

    //设置回调接口
    public interface OnItemClickLitener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(MyTestRecyclerViewAdapter.OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public MyTestRecyclerViewAdapter(List<TestListItem.TItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_test, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mChartNameView.setText(mValues.get(position).chartName);
        holder.mNameView.setText(mValues.get(position).testName);

        //通过为条目设置点击事件触发回调
        if (mOnItemClickLitener != null) {
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickLitener.onItemClick(view, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mChartNameView;
        public final TextView mNameView;
        public TestListItem.TItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mChartNameView = (TextView) view.findViewById(R.id.chart_name);
            mNameView = (TextView) view.findViewById(R.id.test_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}