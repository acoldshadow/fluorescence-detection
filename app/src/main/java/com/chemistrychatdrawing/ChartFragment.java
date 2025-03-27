package com.chemistrychatdrawing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A fragment representing a list of Items.
 */
public class ChartFragment extends Fragment {

    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChartFragment newInstance(int columnCount) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MychartRecyclerViewAdapter adapter=new MychartRecyclerViewAdapter(ChartListItem.ITEMS);
            adapter.setOnItemClickLitener(new MychartRecyclerViewAdapter.OnItemClickLitener() {
                @Override
                public void onItemClick(View view, int position) {
//                    Toast.makeText(context,"这是条目"+ChartListItem.ITEMS.get(position), Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),DrawingActivity.class);
                    intent.putExtra(DrawingActivity.ExtraType,DrawingActivity.LOAD);
                    intent.putExtra(ChartListItem.SLOPE, ChartListItem.ITEMS.get(position).slope);
                    intent.putExtra(ChartListItem.INTERCEPT, ChartListItem.ITEMS.get(position).intercept);
                    intent.putExtra(ChartListItem.R_SQUARED, ChartListItem.ITEMS.get(position).R_squared);
                    intent.putExtra(ChartListItem.POINTNUM, ChartListItem.ITEMS.get(position).pointNum);
                    intent.putExtra(ChartListItem.TIME, ChartListItem.ITEMS.get(position).time);
                    intent.putExtra(ChartListItem.CHARTNAME, ChartListItem.ITEMS.get(position).chartName);
                    String debug= ChartListItem.ITEMS.get(position).blank;
                    intent.putExtra("blank", ChartListItem.ITEMS.get(position).blank);
                    int testPointNum=Integer.parseInt(ChartListItem.ITEMS.get(position).pointNum);
                    for(int i=0;i<testPointNum;i++) {
                        intent.putExtra("pointConcentration"+i, ChartListItem.ITEMS.get(position).testPointConcentration[i]);
                        intent.putExtra("pointValue"+i, ChartListItem.ITEMS.get(position).testPointValue_normalized[i]);
                    }
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
        }
        return view;
    }
}