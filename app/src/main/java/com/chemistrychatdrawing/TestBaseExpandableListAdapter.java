package com.chemistrychatdrawing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chemistrychatdrawing.gridItem.TestCon;

import java.util.ArrayList;
import java.util.List;

public class TestBaseExpandableListAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> gData;
    private ArrayList<ArrayList<TestItem>> iData;
    private Context mContext;

    public TestBaseExpandableListAdapter(ArrayList<String> gData, ArrayList<ArrayList<TestItem>> iData, Context mContext) {
        this.gData = gData;
        this.iData = iData;
        this.mContext = mContext;
    }

    @Override
    public int getGroupCount() {
        return gData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return iData.get(groupPosition).size();
    }

    @Override
    public String getGroup(int groupPosition) {
        return gData.get(groupPosition);
    }

    @Override
    public TestItem getChild(int groupPosition, int childPosition) {
        return iData.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    //取得用于显示给定分组的视图. 这个方法仅返回分组的视图对象
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolderGroup groupHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.test_group_exlist, parent, false);
            groupHolder = new ViewHolderGroup();
            groupHolder.tv_group_name = (TextView) convertView.findViewById(R.id.tv_group_name);
            convertView.setTag(groupHolder);
        }else{
            groupHolder = (ViewHolderGroup) convertView.getTag();
        }
        groupHolder.tv_group_name.setText(gData.get(groupPosition));
        return convertView;
    }

    //取得显示给定分组给定子位置的数据用的视图
    @SuppressLint("DefaultLocale")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolderItem itemHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.test_item_exlist, parent, false);
            itemHolder = new ViewHolderItem();
//            itemHolder.debug=(TextView)convertView.findViewById(R.id.debugtext);
            itemHolder.img_1 = (ImageView) convertView.findViewById(R.id.img_1);
            itemHolder.img_2 = (ImageView) convertView.findViewById(R.id.img_2);
            itemHolder.img_3 = (ImageView) convertView.findViewById(R.id.img_3);
            itemHolder.img_4 = (ImageView) convertView.findViewById(R.id.img_4);
            itemHolder.img_5 = (ImageView) convertView.findViewById(R.id.img_5);
            itemHolder.img_6 = (ImageView) convertView.findViewById(R.id.img_6);
            itemHolder.tv_1=(TextView)convertView.findViewById(R.id.grid_context1);
            itemHolder.tv_2=(TextView)convertView.findViewById(R.id.grid_context2);
            itemHolder.tv_3=(TextView)convertView.findViewById(R.id.grid_context3);
            itemHolder.tv_4=(TextView)convertView.findViewById(R.id.grid_context4);
            itemHolder.tv_5=(TextView)convertView.findViewById(R.id.grid_context5);
            itemHolder.tv_6=(TextView)convertView.findViewById(R.id.grid_context6);
            convertView.setTag(itemHolder);
        }else{
            itemHolder = (ViewHolderItem) convertView.getTag();
        }
        //TODO:获取数据

        //debug
        //itemHolder.img_1.setImage(iData.get(groupPosition).get(childPosition).getiId());
//        itemHolder.debug.setText(String.valueOf(iData.get(groupPosition).get(childPosition).con[0]));
        itemHolder.img_1.setImageBitmap(iData.get(groupPosition).get(childPosition).img[0]);
        itemHolder.img_2.setImageBitmap(iData.get(groupPosition).get(childPosition).img[1]);
        itemHolder.img_3.setImageBitmap(iData.get(groupPosition).get(childPosition).img[2]);
        itemHolder.img_4.setImageBitmap(iData.get(groupPosition).get(childPosition).img[3]);
        itemHolder.img_5.setImageBitmap(iData.get(groupPosition).get(childPosition).img[4]);
        itemHolder.img_6.setImageBitmap(iData.get(groupPosition).get(childPosition).img[5]);
        itemHolder.tv_1.setText(String.format("%.2f", iData.get(groupPosition).get(childPosition).con[0]));
        itemHolder.tv_2.setText(String.format("%.2f", iData.get(groupPosition).get(childPosition).con[1]));
        itemHolder.tv_3.setText(String.format("%.2f", iData.get(groupPosition).get(childPosition).con[2]));
        itemHolder.tv_4.setText(String.format("%.2f", iData.get(groupPosition).get(childPosition).con[3]));
        itemHolder.tv_5.setText(String.format("%.2f", iData.get(groupPosition).get(childPosition).con[4]));
        itemHolder.tv_6.setText(String.format("%.2f", iData.get(groupPosition).get(childPosition).con[5]));

        return convertView;
    }

    //设置子列表是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private static class ViewHolderGroup{
        private TextView tv_group_name;
    }

    private static class ViewHolderItem{
        private ImageView img_1;
        private ImageView img_2;
        private ImageView img_3;
        private ImageView img_4;
        private ImageView img_5;
        private ImageView img_6;
        private TextView tv_1;
        private TextView tv_2;
        private TextView tv_3;
        private TextView tv_4;
        private TextView tv_5;
        private TextView tv_6;
        //debug
        private TextView debug;
    }

}
