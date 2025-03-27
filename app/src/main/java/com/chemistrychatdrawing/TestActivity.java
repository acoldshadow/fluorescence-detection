package com.chemistrychatdrawing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TestActivity extends AppCompatActivity {
    public static final String ExtraType = "NewFlag";   //Activity传值key
    public static final int NEW = 1;
    public static final int LOAD = 0;
    int type=NEW;

    private ArrayList<String> gData = null;
    private ArrayList<ArrayList<TestItem>> iData = null;
    private ArrayList<TestItem> lData = null;
    private Context mContext;
    private ExpandableListView exlist;
    private TestBaseExpandableListAdapter myAdapter = null;

    double slope=0;
    double intercept=0;
    double blank=255;
    int seged_picture_num=0;

   TestData data=new TestData();

    Button btn_backToChart;
    Button btn_save;
    private double R_squared;
    private int chartPointNum;
    private String chartTime;
    private double[] testPointConcentration;
    private double[] testPointValue_normalized;

    public static class TestData implements Serializable {
        private static final long serialVersionUID = 1L;
        String chartName=new String();
        String testName=new String();
//        ArrayList<ArrayList<Byte>> bitmapBytesArray=new ArrayList<ArrayList<Byte>>();
        Byte[][] bitmapBytes;
//        ArrayList<Double> valueArray=new ArrayList<Double>();
        Double[] values;
    }

    Bitmap[] bitmaps;



    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mContext = TestActivity.this;
        exlist = (ExpandableListView) findViewById(R.id.exlist);
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.TestToolbar);
        setSupportActionBar(myChildToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        slope=Double.parseDouble(intent.getExtras().getString("slope"));
        intercept=Double.parseDouble(intent.getExtras().getString("intercept"));
        blank=Double.parseDouble(intent.getExtras().getString("blank"));
        data.chartName=intent.getExtras().getString("chartName");
        R_squared=Double.parseDouble(intent.getExtras().getString("R_squared"));
        chartPointNum=Integer.parseInt(intent.getExtras().getString("pointNum"));
        chartTime=intent.getExtras().getString("time");
        testPointConcentration=new double[chartPointNum];
        testPointValue_normalized=new double[chartPointNum];
        for(int i=0;i<chartPointNum;i++)
        {
            testPointConcentration[i]=Double.parseDouble(intent.getExtras().getString("pointConcentration"+i));
            testPointValue_normalized[i]=Double.parseDouble(intent.getExtras().getString("pointValue"+i));
        }

        type=intent.getExtras().getInt(ExtraType);
        if(type==NEW) {
            Uri IMG_URI = Uri.parse(intent.getExtras().getString("IMG_URL"));
            Bitmap bitmap = null;
            try {
//              bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), IMG_URI);
                bitmap = BitmapFactory.decodeStream(new FileInputStream(uriToFileApiQ(IMG_URI)));
//            Bitmap bitmap =MediaStore.Images.Media.getBitmap(getContentResolver(),IMG_URI);
            } catch (IOException e) {
                Log.e("ERROR", "Bitmap读取错误！");
                e.printStackTrace();
            }
            double[] BitmapDoubleValue=new double[bitmap.getWidth()*bitmap.getHeight()];
            BitmapDoubleValue = convertGreyImg(bitmap);
//        image.setImageBitmap(blankBitmap);
            int[] labeledBitmap = segPicture(BitmapDoubleValue,bitmap.getWidth(),bitmap.getHeight());
//        Bitmap labeledBlankBitmap=segPicture(blankBitmap);
//        image.setImageBitmap(labeledBlankBitmap);
            labeledBitmap = postSegPicture(labeledBitmap,bitmap.getWidth(),bitmap.getHeight());
            bitmaps = getBitmaps(bitmap, labeledBitmap);
            data.bitmapBytes=new Byte[bitmaps.length][];
            for(int i=0;i<bitmaps.length;i++)
            {
                data.bitmapBytes[i]=Bitmap2Bytes(bitmaps[i]);
            }
            data.values = new Double[24];
            for (int i = 0; i < 4; i++) {
                double blank=0;
                for(int j=0;j<6;j++) {
                    double grey = getValue(BitmapDoubleValue, labeledBitmap, i*6+j + 1,bitmap.getWidth(),bitmap.getHeight());
                    if(j==0)
                    {
                        blank=grey;
                    }
                    double grey_norm = (blank-grey) / blank;
                    data.values[i*6+j] = (grey_norm - intercept)/slope;
                }
            }
            CameraActivity.IMG_File.delete();
        }else if(type==LOAD){
            bitmaps = new Bitmap[24];
            data.values = new Double[24];
            data.bitmapBytes=new Byte[24][];
            for(int i=0;i<24;i++)
            {
                byte[] tempbytes= (byte[]) intent.getExtras().get("bitmapByte"+i);
                data.bitmapBytes[i]=new Byte[tempbytes.length];
                for(int j=0;j<tempbytes.length;j++)
                {
                    data.bitmapBytes[i][j]=tempbytes[j];
                }
                data.values[i]=(Double) intent.getExtras().get("con"+i);
                bitmaps[i]=getPicFromBytes(data.bitmapBytes[i],null);
            }
            data.testName=intent.getExtras().getString(TestListItem.TESTNAME);
        }else{
            TestActivity.this.finish();
        }

        //数据准备
        gData = new ArrayList<String>();
        iData = new ArrayList<ArrayList<TestItem>>();
        gData.add(new String("第一组"));
        gData.add(new String("第二组"));
        gData.add(new String("第三组"));
        gData.add(new String("第四组"));

//        //AD组
//        lData = new ArrayList<TestItem>();
//        lData.add(new TestItem(1.1));
//        lData.add(new TestItem(1.2));
//        iData.add(lData);
//        //AP组
//        lData = new ArrayList<TestItem>();
//        lData.add(new TestItem(2.1));
//        lData.add(new TestItem(2.2));
//        iData.add(lData);
//        //TANK组
//        lData = new ArrayList<TestItem>();
//        lData.add(new TestItem(3.1));
//        lData.add(new TestItem(3.2));
//        iData.add(lData);

        for(int i=0;i<4;i++) {
            lData = new ArrayList<TestItem>();
            Bitmap[] curBitmaps=new Bitmap[6];
            Double[] curValues=new Double[6];
            for(int j=0;j<6;j++)
            {
                curBitmaps[j]=bitmaps[i*6+j];
                curValues[j]=data.values[i*6+j];
            }
            lData.add(new TestItem(curBitmaps,curValues));
            iData.add(lData);
        }


        myAdapter = new TestBaseExpandableListAdapter(gData,iData,mContext);
        exlist.setAdapter(myAdapter);
//        //为列表设置点击事件
//        exlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                Toast.makeText(mContext, "你点击了：" + iData.get(groupPosition).get(childPosition).con[0], Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });

        btn_backToChart=(Button)findViewById(R.id.backToChartBtn);
        btn_save=(Button)findViewById(R.id.testSaveBtn);

        btn_backToChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TestActivity.this,DrawingActivity.class);
                intent.putExtra(DrawingActivity.ExtraType,DrawingActivity.LOAD);
                intent.putExtra("slope",String.valueOf(slope));
                intent.putExtra("intercept",String.valueOf(intercept));
                intent.putExtra("blank",String.valueOf(blank));
                intent.putExtra("name",data.chartName);
                intent.putExtra("R_squared",String.valueOf(R_squared));
                intent.putExtra("pointNum",String.valueOf(chartPointNum));
                intent.putExtra("time",chartTime);
                for(int i=0;i<chartPointNum;i++) {
                    intent.putExtra("pointConcentration"+i,String.valueOf(testPointConcentration[i]));
                    intent.putExtra("pointValue"+i,String.valueOf(testPointValue_normalized[i]));
                }

                startActivity(intent);
                TestActivity.this.finish();
            }
        });
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(TestActivity.this);
                AlertDialog.Builder dialog = new AlertDialog.Builder(TestActivity.this);
                dialog.setTitle("保存-输入名字");
                dialog.setView(editText);
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        data.testName=editText.getText().toString();
                        save();
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
            }
        });
    }

    private Bitmap[] getBitmaps(Bitmap img, int[] labeledPixels) {
        Bitmap[] bitmaps=new Bitmap[24];

        int width = img.getWidth(); //获取位图的宽
        int height = img.getHeight(); //获取位图的高
        //Debug
//        Toast.makeText(this,"宽度: "+width,Toast.LENGTH_SHORT).show();
//        Toast.makeText(this,"高度: "+height,Toast.LENGTH_SHORT).show();
        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
        img.getPixels(pixels, 0, width, 0, 0, width, height);

        for(int n=1;n<=24;n++)
        {
            int max_i=0,min_i=height,max_j=0,min_j=width;
            for(int i=0;i<height;i++)
            {
                for(int j=0;j<width;j++)
                {
                    if(labeledPixels[i*width+j]==n) {
//                    targetPixels.add(i * width + j);
                        if(i<min_i)
                            min_i=i;
                        if(i>max_i)
                            max_i=i;
                        if(j<min_j)
                            min_j=j;
                        if(j>max_j)
                            max_j=j;
                    }
                }
            }
            int curWidth=max_j-min_j;
            int curHeight=max_i-min_i;
            int[] curPixels=new int[curHeight*curWidth];
            for(int i=0;i<curHeight;i++)
            {
                for(int j=0;j<curWidth;j++)
                {
                    curPixels[i*curWidth+j]=pixels[(min_i+i)*width+min_j+j];
                }
            }
            bitmaps[n-1]=Bitmap.createBitmap(curWidth, curHeight, Bitmap.Config.RGB_565);
            bitmaps[n-1].setPixels(curPixels, 0, curWidth, 0, 0, curWidth, curHeight);
        }
        return bitmaps;
    }


    /**
     * 将图片内容解析成字节数组
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }
    /**
     * 将字节数组转换为ImageView可调用的Bitmap对象
     * @param Bytes
     * @param opts
     * @return Bitmap
     */
    public static Bitmap getPicFromBytes(Byte[] Bytes,
                                         BitmapFactory.Options opts) {
        byte[] bytes=new byte[Bytes.length];
        for(int i=0;i<Bytes.length;i++)
        {
            bytes[i]=Bytes[i];
        }
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }
    /**
     * 图片缩放
     * @param bitmap 对象
     * @param w 要缩放的宽度
     * @param h 要缩放的高度
     * @return newBmp 新 Bitmap对象
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        return newBmp;
    }

    /**
     * 把Bitmap转Byte
     * @Author HEH
     * @EditTime 2010-07-19 上午11:45:56
     */
    public static Byte[] Bitmap2Bytes(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b=baos.toByteArray();
        Byte[] B=new Byte[b.length];
        for(int i=0;i<b.length;i++){
            B[i]=Byte.valueOf(b[i]);
        }
        return B;
    }
    /**
     * 把字节数组保存为一个文件
     * @Author HEH
     * @EditTime 2010-07-19 上午11:45:56
     */
    public static File getFileFromBytes(byte[] b, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 将彩色图转换为灰度图
     * @param img 位图
     * @return 返回转换好的位图
     */
    public double[] convertGreyImg(Bitmap img) {
        int width = img.getWidth(); //获取位图的宽
        int height = img.getHeight(); //获取位图的高

//        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组
//        img.getPixels(pixels, 0, width, 0, 0, width, height);
//        int alpha = 0xFF << 24;
//        for(int i = 0; i < height; i++) {
//            for(int j = 0; j < width; j++) {
//                int grey = pixels[width * i + j];
//                int red = ((grey & 0x00FF0000 ) >> 16);
//                int green = ((grey & 0x0000FF00) >> 8);
//                int blue = (grey & 0x000000FF);
//                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
//                grey = alpha | (grey << 16) | (grey << 8) | grey;
//                pixels[width * i + j] = grey;
//            }
//        }
//        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        result.setPixels(pixels, 0, width, 0, 0, width, height);
//        return result;

        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组
        double[] result = new double[width*height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                int red = ((grey & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                double greyD = ((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
//                grey = alpha | (grey << 16) | (grey << 8) | grey;
//                pixels[width * i + j] = grey;
                result[width*i+j]=greyD;
            }
        }
//        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 将彩色图转换为分割标记图
     * @param pixels double值图
     * @return 返回分割标记好的位图
     */
    public int[] segPicture(double[] pixels,int width,int height) {
        Queue<Integer> queue = new LinkedList<Integer>();
//        int width = img.getWidth(); //获取位图的宽
//        int height = img.getHeight(); //获取位图的高
//        Toast.makeText(this,"宽度: "+width,Toast.LENGTH_SHORT).show();
//        Toast.makeText(this,"高度: "+height,Toast.LENGTH_SHORT).show();
//        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
//        img.getPixels(pixels, 0, width, 0, 0, width, height);
        //标记矩阵
        int[] labeledPixels = new int[width * height];
        //获取像素灰度：
        //gray=(pixels[i*width+j]&0x000000FF);


        int labelLimit = 25;
        int queueLimit= 13;
//        for (int i = 0; i < width * height; i++) {
//            labeledPixels[i] = 0;
//            pixels[i]=pixels[i]&0x000000FF;
//        }

        int label_num=0;
        for (int i = 0; i < height; i++)
        {
            for(int j=0;j<width;j++)
            {
                if(pixels[i*width+j]>labelLimit&&labeledPixels[i*width+j]==0)
                {
                    queue.offer(i*width+j);
                    label_num++;
                    labeledPixels[i*width+j]=label_num;
                    while(!queue.isEmpty())
                    {
                        int temp=queue.poll();
                        int ii=temp/width;
                        int jj=temp%width;
                        if(ii-1>=0&&jj-1>=0)
                        {
                            if(pixels[(ii-1)*width+(jj-1)]>queueLimit&&labeledPixels[(ii-1)*width+(jj-1)]==0)
                            {
                                queue.offer((ii-1)*width+jj-1);
                                labeledPixels[(ii-1)*width+jj-1]=label_num;
                            }
                        }
                        if(ii-1>=0&&jj+1<width)
                        {
                            if(pixels[(ii-1)*width+(jj+1)]>queueLimit&&labeledPixels[(ii-1)*width+(jj+1)]==0)
                            {
                                queue.offer((ii-1)*width+jj+1);
                                labeledPixels[(ii-1)*width+jj+1]=label_num;
                            }
                        }
                        if(ii+1<height&&jj+1<width)
                        {
                            if(pixels[(ii+1)*width+(jj+1)]>queueLimit&&labeledPixels[(ii+1)*width+(jj+1)]==0)
                            {
                                queue.offer((ii+1)*width+jj+1);
                                labeledPixels[(ii+1)*width+jj+1]=label_num;
                            }
                        }
                        if(ii+1<height&&jj-1>=0)
                        {
                            if(pixels[(ii+1)*width+(jj-1)]>queueLimit&&labeledPixels[(ii+1)*width+(jj-1)]==0)
                            {
                                queue.offer((ii+1)*width+jj-1);
                                labeledPixels[(ii+1)*width+jj-1]=label_num;
                            }
                        }
                        if(ii-1>=0)
                        {
                            if(pixels[(ii-1)*width+jj]>queueLimit&&labeledPixels[(ii-1)*width+jj]==0)
                            {
                                queue.offer((ii-1)*width+jj);
                                labeledPixels[(ii-1)*width+jj]=label_num;
                            }
                        }
                        if(jj+1<width)
                        {
                            if(pixels[ii*width+(jj+1)]>queueLimit&&labeledPixels[ii*width+(jj+1)]==0)
                            {
                                queue.offer(ii*width+jj+1);
                                labeledPixels[ii*width+jj+1]=label_num;
                            }
                        }
                        if(ii+1<height)
                        {
                            if(pixels[(ii+1)*width+jj]>queueLimit&&labeledPixels[(ii+1)*width+jj]==0)
                            {
                                queue.offer((ii+1)*width+jj);
                                labeledPixels[(ii+1)*width+jj]=label_num;
                            }
                        }
                        if(jj-1>=0)
                        {
                            if(pixels[ii*width+(jj-1)]>queueLimit&&labeledPixels[ii*width+(jj-1)]==0)
                            {
                                queue.offer(ii*width+jj-1);
                                labeledPixels[ii*width+jj-1]=label_num;
                            }
                        }
                    }
                }
            }
        }
        seged_picture_num =label_num;

        //Debug for show
//        int alpha = 0xFF << 24;
//        for(int i = 0; i < height; i++) {
//            for(int j = 0; j < width; j++) {
//                if(labeledPixels[width * i + j]!=0) {
////                    labeledPixels[width * i + j] = alpha | 255<< 16|255<<8|255;
//                    int temp=(230/label_num*labeledPixels[width * i + j])+20;
//                    labeledPixels[width * i + j] = alpha | temp << 16|temp<<8|temp;
//                }else{
//                    labeledPixels[width * i + j] = alpha | 0 << 16|0<<8|0;
//                }
//            }
//        }

        //Debug标记矩阵转换成pixels返回
//        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        result.setPixels(labeledPixels, 0, width, 0, 0, width, height);
//        return result;
        return labeledPixels;
    }

    public int[] postSegPicture(int[] labeledPixels,int width,int height) {
//        int width = img.getWidth(); //获取位图的宽
//        int height = img.getHeight(); //获取位图的高
//        //Debug
////        Toast.makeText(this,"宽度: "+width,Toast.LENGTH_SHORT).show();
////        Toast.makeText(this,"高度: "+height,Toast.LENGTH_SHORT).show();
//        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
//        img.getPixels(pixels, 0, width, 0, 0, width, height);

        List<tempForSort> centerPoints=new ArrayList<tempForSort>();
//        getBiggest24(img,labeledBitmap);
        for(int n=1;n<=24;n++)
        {
            int max_i=0,min_i=height,max_j=0,min_j=width;
            for(int i=0;i<height;i++)
            {
                for(int j=0;j<width;j++)
                {
                    if(labeledPixels[i*width+j]==n) {
//                    targetPixels.add(i * width + j);
                        if(i<min_i)
                            min_i=i;
                        if(i>max_i)
                            max_i=i;
                        if(j<min_j)
                            min_j=j;
                        if(j>max_j)
                            max_j=j;
                    }
                }
            }
            int center_i=(max_i+min_i)/2;
            int center_j=(max_j+min_j)/2;
            centerPoints.add(new tempForSort(center_i,center_j,n));
        }
        Collections.sort(centerPoints);
        for(int i=0;i<centerPoints.size();i++)
        {
            centerPoints.get(i).XOrYFlag=1;
        }

        for(int i=0;i<4;i++)
        {
            List<tempForSort> tempCenterPoints=new ArrayList<tempForSort>();
            for(int j=0;j<6;j++)
            {
                tempCenterPoints.add(centerPoints.get(i*6+j));
            }
            Collections.sort(tempCenterPoints);
            for(int j=0;j<6;j++)
            {
                centerPoints.set(i*6+j,tempCenterPoints.get(j));
            }
        }

        int[] trans=new int[centerPoints.size()+1];
        for(int i=0;i<centerPoints.size();i++)
        {
            trans[centerPoints.get(i).label]=i+1;
        }
        trans[0]=0;

        for(int i=0;i<height;i++)
        {
            for(int j=0;j<width;j++)
            {
                if(labeledPixels[i*width+j]<=24)
                    labeledPixels[i*width+j]=trans[labeledPixels[i*width+j]];
            }
        }
        return labeledPixels;
    }

    public class tempForSort implements Comparable<tempForSort>{
        public int x;
        public int y;
        public int label;
        public int XOrYFlag=0;

        public tempForSort(int x,int y,int label){
            this.x=x;
            this.y=y;
            this.label=label;
            XOrYFlag=0;
        }

        @Override
        public int compareTo(tempForSort o) {
            if(XOrYFlag==0)
            {
                return x-o.x;
            }else{
                return y-o.y;
            }

        }
    }

    /**
     * @param pixels 分割标记图
     * @return 取两个矩形取平均
     */
    public double getValue(double[] pixels,int[] labeledPixels,int index,int width,int height){
//        int width = img.getWidth(); //获取位图的宽
//        int height = img.getHeight(); //获取位图的高
        //Debug
//        Toast.makeText(this,"宽度: "+width,Toast.LENGTH_SHORT).show();
//        Toast.makeText(this,"高度: "+height,Toast.LENGTH_SHORT).show();
//        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
//        img.getPixels(pixels, 0, width, 0, 0, width, height);
//        //获取像素灰度：
//        //gray=(pixels[i*width+j]&0x000000FF);
//        for (int i = 0; i < width * height; i++) {
//            pixels[i]=pixels[i]&0x000000FF;
//        }

//        List<Integer> targetPixels=new ArrayList<Integer>();
        int max_i=0,min_i=height,max_j=0,min_j=width;
        for(int i=0;i<height;i++)
        {
            for(int j=0;j<width;j++)
            {
                if(labeledPixels[i*width+j]==index) {
//                    targetPixels.add(i * width + j);
                    if(i<min_i)
                        min_i=i;
                    if(i>max_i)
                        max_i=i;
                    if(j<min_j)
                        min_j=j;
                    if(j>max_j)
                        max_j=j;
                }
            }
        }
        int radius=(max_i-min_i)<(max_j-min_j)?(max_i-min_i)/2:(max_j-min_j)/2;
        int center_i=(max_i+min_i)/2;
        int center_j=(max_j+min_j)/2;
        int lowest_i=center_i,lowest_j=center_j;
        double lowest=255;
        for(int i=center_i-radius/3;i<center_i+radius/3;i++)
        {
            for(int j=center_j-radius/3;j<center_j+radius/3;j++)
            {
                if(pixels[i*width+j]<lowest)
                {
                    lowest=pixels[i*width+j];
                    lowest_i=i;
                    lowest_j=j;
                }
            }
        }
        double sum=0;
        for(int i=lowest_i-radius/3;i<lowest_i+radius/3;i++)
        {
            for(int j=lowest_j-radius/3;j<lowest_j+radius/3;j++)
            {
                sum+=(double)pixels[i*width+j];
            }
        }
        return sum/(double)((radius/3*2)*(radius/3*2));
    }

    public void save(){
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream oos=null;
        try {
            File dir = new File(getExternalFilesDir(""),"Tests");
            if( !dir.exists()){
                dir.mkdirs();}
            File file = new File(dir,data.testName);
            if(!file.exists()){
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            oos=new ObjectOutputStream(fileOutputStream);
            oos.writeObject(data);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(oos!=null){
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public File uriToFileApiQ(Uri uri) {
        File file = null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = this.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try {
                    InputStream is = contentResolver.openInputStream(uri);
                    File cache = new File(this.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    FileUtils.copy(is, fos);
                    file = cache;
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }
}