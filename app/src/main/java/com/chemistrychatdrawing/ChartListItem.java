package com.chemistrychatdrawing;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartListItem {
    public static final String SLOPE="slope";
    public static final String INTERCEPT="intercept";
    public static final String R_SQUARED="R_squared";
    public static final String POINTNUM="pointNum";
    public static final String TIME="time";
    public static final String CHARTNAME ="name";

    /**
     * An array of sample (dummy) items.
     */
    public static final List<CItem> ITEMS = new ArrayList<CItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, CItem> ITEM_MAP = new HashMap<String, CItem>();

    private static final int COUNT = 25;

//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
//    }

    private static void addItem(CItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.chartName, item);
    }

    public static void addItem(String time, String name,String slope,String intercept,String R_squared,String pointNum,String blank,String[] Concentrations,String[] Values){
        CItem item=new CItem(time, name, slope, intercept, R_squared, pointNum,blank,Concentrations,Values);
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
    public static class CItem {
        public final String time;
        public final String chartName;
        public final String slope;
        public final String pointNum;
        public final String intercept;
        public final String R_squared;
        public String blank;
        public String[] testPointConcentration;
        public String[] testPointValue_normalized;

        public CItem(String time, String chartName, String slope, String intercept, String R_squared, String pointNum, String blank, String[] Concentrations, String[] Values) {
            this.time = time;
            this.chartName = chartName;
            this.slope=slope;
            this.intercept=intercept;
            this.R_squared=R_squared;
            this.pointNum=pointNum;
            this.blank=blank;
            testPointConcentration=Concentrations;
            testPointValue_normalized=Values;
        }

        public CItem(CItem cItem) {
            this.time = cItem.time;
            this.chartName = cItem.chartName;
            this.slope=cItem.slope;
            this.intercept=cItem.intercept;
            this.R_squared=cItem.R_squared;
            this.pointNum=cItem.pointNum;
            this.blank=cItem.blank;
            testPointConcentration=cItem.testPointConcentration;
            testPointValue_normalized=cItem.testPointValue_normalized;
        }

        @Override
        public String toString() {
            return chartName;
        }
    }
}
