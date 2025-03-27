package com.chemistrychatdrawing;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ChartListItem.CItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MychartRecyclerViewAdapter extends RecyclerView.Adapter<MychartRecyclerViewAdapter.ViewHolder> {

    private final List<ChartListItem.CItem> mValues;
    private OnItemClickLitener  mOnItemClickLitener;

    //设置回调接口
    public interface OnItemClickLitener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener){
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public MychartRecyclerViewAdapter(List<ChartListItem.CItem> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTimeView.setText(mValues.get(position).time);
        holder.mNameView.setText(mValues.get(position).chartName);

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
        public final TextView mTimeView;
        public final TextView mNameView;
        public ChartListItem.CItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTimeView = (TextView) view.findViewById(R.id.chartTime);
            mNameView = (TextView) view.findViewById(R.id.chartName);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}