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

import com.chemistrychatdrawing.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 */
public class TestFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TestFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TestFragment newInstance(int columnCount) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_test_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            MyTestRecyclerViewAdapter adapter=new MyTestRecyclerViewAdapter(TestListItem.ITEMS);
            adapter.setOnItemClickLitener(new MyTestRecyclerViewAdapter.OnItemClickLitener(){
                @Override
                public void onItemClick(View view, int position) {
//                    Toast.makeText(context,"这是条目"+ChartListItem.ITEMS.get(position), Toast.LENGTH_LONG).show();
                    Intent intent=new Intent(getActivity(),TestActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putInt(TestActivity.ExtraType,TestActivity.LOAD);
                    bundle.putString(ChartListItem.SLOPE, TestListItem.ITEMS.get(position).slope);
                    bundle.putString(ChartListItem.INTERCEPT, TestListItem.ITEMS.get(position).intercept);
                    bundle.putString(ChartListItem.R_SQUARED, TestListItem.ITEMS.get(position).R_squared);
                    bundle.putString(ChartListItem.POINTNUM, TestListItem.ITEMS.get(position).pointNum);
                    bundle.putString(ChartListItem.TIME, TestListItem.ITEMS.get(position).time);
                    bundle.putString(ChartListItem.CHARTNAME, TestListItem.ITEMS.get(position).chartName);
//                    String debug= TestListItem.ITEMS.get(position).blank;
                    bundle.putString("blank", TestListItem.ITEMS.get(position).blank);
                    int testPointNum=Integer.parseInt(TestListItem.ITEMS.get(position).pointNum);
                    for(int i=0;i<testPointNum;i++) {
                        bundle.putString("pointConcentration"+i,TestListItem.ITEMS.get(position).testPointConcentration[i]);
                        bundle.putString("pointValue"+i, TestListItem.ITEMS.get(position).testPointValue_normalized[i]);
                    }

                    bundle.putString(TestListItem.TESTNAME, TestListItem.ITEMS.get(position).testName);
                    for(int i=0;i<24;i++)
                    {
                        Byte[] tempBypes=TestListItem.ITEMS.get(position).img[i];
                        byte[] tempbypes=new byte[tempBypes.length];
                        for(int j=0;j<tempBypes.length;j++){
                            tempbypes[j]=tempBypes[j];
                        }
                        bundle.putByteArray("bitmapByte"+i,tempbypes);
                        bundle.putDouble("con"+i,TestListItem.ITEMS.get(position).con[i]);
                    }
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });
            recyclerView.setAdapter(adapter);
        }
        return view;
    }
}