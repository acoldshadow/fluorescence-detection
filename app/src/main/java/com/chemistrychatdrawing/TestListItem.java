package com.chemistrychatdrawing;


import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestListItem {
    public static final String TESTNAME="name";

    /**
     * An array of sample (dummy) items.
     */
    public static final List<TItem> ITEMS = new ArrayList<TItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, TItem> ITEM_MAP = new HashMap<String, TItem>();

    private static final int COUNT = 25;

//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
//    }

    private static void addItem(TItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.testName, item);
    }

    public static void addItem(ChartListItem.CItem citem,String tName,Byte[][] b,Double[] c){
        TItem item=new TItem(citem,tName,b,c);
        ITEMS.add(item);
        ITEM_MAP.put(item.chartName,item);
    }

    public static void clear(){
        ITEMS.clear();
        ITEM_MAP.clear();
    }

//    private static DummyContent.DummyItem createDummyItem(int position) {
//        return new DummyContent.DummyItem(String.valueOf(position), "Item " + position, makeDetails(position));
//    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class TItem extends ChartListItem.CItem {
        public String testName;
        public Byte[][] img=new Byte[24][];

        public Double[] con=new Double[24];

        public TItem(ChartListItem.CItem cItem,String tName, Byte[][] b,Double[] c){
            super(cItem);
            testName=tName;
            img=b;
            con=c;
        }

        @Override
        public String toString() {
            return testName;
        }
    }
}
