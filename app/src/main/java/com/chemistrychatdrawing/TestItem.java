package com.chemistrychatdrawing;

import android.graphics.Bitmap;

public class TestItem {
    public Bitmap[] img=new Bitmap[6];

    public Double[] con=new Double[6];

    /*
    Debug!
     */
    public TestItem(double con){
        this.con[0]=con;
    }

    public TestItem(Bitmap[] b,Double[] d){
        img=b;
        con=d;
    }
}
