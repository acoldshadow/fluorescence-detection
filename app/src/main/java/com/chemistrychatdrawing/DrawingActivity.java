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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class DrawingActivity extends AppCompatActivity {
    public static final String ExtraType = "NewFlag";   //Activity传值key
    public static final int NEW = 1;
    public static final int LOAD = 0;
    int type=NEW;

    Intent intent;
    View contentView;
    PopupWindow popupWindow;

    double blankValue;
    double[] testPointConcentration;
    double[] testPointValue;
    double[] testPointValue_normalized;
    int testPointNum;
    int seged_picture_num;

    File blankPointFile;

    LineChart line;

    TextView slope_textView;
    TextView intercept_textView;
    TextView R_squared_textView;
    TextView pointNum_textView;
    double slope=0;
    double intercept=0;
    double R_squared=0;
    String name=null;

    Button changeY_btn;
    Button save_btn;
    Button test_btn;

    boolean yShowing=true;

    Map<String,String> dataMap=new HashMap<String, String>();


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.DrawToolbar);
        setSupportActionBar(myChildToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        slope_textView=(TextView)findViewById(R.id.slope);
        intercept_textView=(TextView)findViewById(R.id.intercept);
        R_squared_textView=(TextView)findViewById(R.id.R_squared);
        pointNum_textView=(TextView)findViewById(R.id.num);
        changeY_btn=(Button)findViewById(R.id.changeY);
        save_btn=(Button)findViewById(R.id.save_btn);
        test_btn=(Button)findViewById(R.id.test_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = new EditText(DrawingActivity.this);
                AlertDialog.Builder dialog = new AlertDialog.Builder(DrawingActivity.this);
                dialog.setTitle("保存-输入名字");
                dialog.setView(editText);
                dialog.setCancelable(false);
                dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name=editText.getText().toString();
                        dataMap.put(ChartListItem.CHARTNAME,name);
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
        test_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name==null)
                {
                    Toast.makeText(DrawingActivity.this,"请先保存",Toast.LENGTH_SHORT).show();
                }else {
                    showPopWindow(-1);
                }
            }
        });

        changeY_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(yShowing==true)
                {
                    changeY_btn.setText("显示Y轴");
                    yShowing=false;

                    line.getAxisLeft().setDrawLabels(false);
                    line.invalidate();
                }else{
                    changeY_btn.setText("隐藏Y轴");
                    yShowing=true;

                    line.getAxisLeft().setDrawLabels(true);
                    line.invalidate();
                }
            }
        });

        line=(LineChart)findViewById(R.id.line);
        line.setNoDataText("请输入数据");

        line.setExtraRightOffset(25f);
        line.setExtraLeftOffset(20f);
        line.setExtraTopOffset(0f);
        line.setExtraBottomOffset(40f);

        //图像背景色，默认灰色
//        line.setDrawGridBackground(true);
        //绘制边框
        line.setDrawBorders(true);
        line.setBorderColor(Color.parseColor("#FF000000"));
        line.setBorderWidth(3);

        //   X轴所在位置   默认为上面
        line.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        line.getXAxis().setTextSize(18);
        line.getAxisLeft().setSpaceBottom(0);
        line.getAxisLeft().setTextSize(18);
        //取消绘制背景十字线
        line.getAxisLeft().setDrawGridLines(false);
        line.getXAxis().setDrawGridLines(false);
        //隐藏右边的Y轴
        line.getAxisRight().setEnabled(false);
        line.getDescription().setEnabled(false);
        line.getLegend().setEnabled(false);

        //声明点击提示框
        CustomMarkerView mv = new CustomMarkerView(DrawingActivity.this, R.layout.chart_marker_view);
        //设置曲线的点击提示框
        line.setMarker(mv);

        initPopupWindow();
//        image=(ImageView)findViewById(R.id.picture);
//        testButton=(Button)findViewById(R.id.test_btn);
//        testButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                image.setImageBitmap(segPicture(((BitmapDrawable)image.getDrawable()).getBitmap()));
//            }
//        });

        intent=getIntent();
        type=intent.getExtras().getInt(ExtraType);
        if(type==NEW) {
            Uri IMG_URI = Uri.parse(intent.getExtras().getString("IMG_URL"));
            testPointNum = intent.getExtras().getInt("point_num");
            blankPointFile = uriToFileApiQ(IMG_URI);
//        image.setImageURI(IMG_URL);
            Bitmap blankBitmap = null;
            try {
//                blankBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), IMG_URI);
            blankBitmap = BitmapFactory.decodeStream(new FileInputStream(uriToFileApiQ(IMG_URI)));
//            Bitmap bitmap =MediaStore.Images.Media.getBitmap(getContentResolver(),IMG_URI);
            } catch (IOException e) {
                Log.e("ERROR", "Bitmap读取错误！");
                e.printStackTrace();
            }
            double[] BitmapDoubleValue=new double[blankBitmap.getWidth()*blankBitmap.getHeight()];
            BitmapDoubleValue = convertGreyImg(blankBitmap);
//        image.setImageBitmap(blankBitmap);
            int[] labeledBlankBitmap = segPicture(BitmapDoubleValue,blankBitmap.getWidth(),blankBitmap.getHeight());
//        Bitmap labeledBlankBitmap=segPicture(blankBitmap);
//        image.setImageBitmap(labeledBlankBitmap);
            blankValue = getValue(BitmapDoubleValue, labeledBlankBitmap, 1,blankBitmap.getWidth(),blankBitmap.getHeight());
            blankPointFile.delete();

            testPointConcentration =new double[testPointNum];
            testPointValue=new double[testPointNum];
            testPointValue_normalized=new double[testPointNum];

            getPoints(0);
        }else if(type==LOAD){
            slope=Double.parseDouble(intent.getExtras().getString(ChartListItem.SLOPE));
            intercept=Double.parseDouble(intent.getExtras().getString(ChartListItem.INTERCEPT));
            R_squared=Double.parseDouble(intent.getExtras().getString(ChartListItem.R_SQUARED));
            testPointNum=Integer.parseInt(intent.getExtras().getString(ChartListItem.POINTNUM));
            String time=intent.getExtras().getString(ChartListItem.TIME);
            name=intent.getExtras().getString(ChartListItem.CHARTNAME);
            blankValue=Double.parseDouble(intent.getExtras().getString("blank"));

            dataMap.put(ChartListItem.SLOPE,String.valueOf(slope));
            dataMap.put(ChartListItem.INTERCEPT,String.valueOf(intercept));
            dataMap.put(ChartListItem.R_SQUARED,String.valueOf(R_squared));
            dataMap.put(ChartListItem.POINTNUM,String.valueOf(testPointNum));
            dataMap.put(ChartListItem.TIME,time);
            dataMap.put(ChartListItem.CHARTNAME,name);
            dataMap.put("blank",String.valueOf(blankValue));

            testPointConcentration =new double[testPointNum];
            testPointValue=new double[testPointNum];
            testPointValue_normalized=new double[testPointNum];

            for(int i=0;i<testPointNum;i++) {
                testPointConcentration[i]=Double.parseDouble(intent.getExtras().getString("pointConcentration"+i));
                testPointValue_normalized[i]=Double.parseDouble(intent.getExtras().getString("pointValue"+i));
            }

            draw();
        }else{
            DrawingActivity.this.finish();
        }


    }

    private void initPopupWindow() {
        //要在布局中显示的布局
        contentView = LayoutInflater.from(this).inflate(R.layout.selectpicture, null, false);
        //实例化PopupWindow并设置宽高
        popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //点击外部消失，这里因为PopupWindow填充了整个窗口，所以这句代码就没用了
        popupWindow.setOutsideTouchable(true);
        //设置可以点击
        popupWindow.setTouchable(true);
    }

    private void showPopWindow(int pointIndex) {
        View rootview = LayoutInflater.from(DrawingActivity.this).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
        TextView cancel_button = (TextView) contentView.findViewById(R.id.cancel);
        TextView camera_button = (TextView) contentView.findViewById(R.id.open_from_camera);
        TextView album_button = (TextView) contentView.findViewById(R.id.open_album);
        cancel_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
            camera_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(DrawingActivity.this, CameraActivity.class).putExtra(CameraActivity.ExtraType, CameraActivity.CAMERA);
                    intent.putExtra("pointIndex",pointIndex);
                    startActivity(intent);
                    popupWindow.dismiss();
                }
        });
        album_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DrawingActivity.this, CameraActivity.class).putExtra(CameraActivity.ExtraType, CameraActivity.PHOTO);
                intent.putExtra("pointIndex",pointIndex);
                startActivity(intent);
                popupWindow.dismiss();
            }
        });
    }

    public void getPoints(int i){
        final EditText editText = new EditText(DrawingActivity.this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(DrawingActivity.this);
        dialog.setTitle("选择标准点浓度(μmol/L)（"+ (i+1) +"/"+ testPointNum +"）");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        dialog.setView(editText);

        dialog.setCancelable(false);
        dialog.setPositiveButton("选择图像", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                testPointConcentration[i]=Double.parseDouble(editText.getText().toString());
                showPopWindow(i);
            }
        });
        dialog.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(i>0)
                    getPoints(i-1);
                else
                    finish();
            }
        });
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        //获得相册、相机返回的结果，并显示
        if (CameraActivity.LISTENING) {
            Log.e("TAG", "返回的Uri结果：" + CameraActivity.IMG_URI);
            Log.e("TAG", "返回的File结果：" + CameraActivity.IMG_File.getPath());
            CameraActivity.LISTENING = false;   //关闭获取结果
            int ind=CameraActivity.pointNum;



            if(ind!=-1) {
                Bitmap bitmap=null;
                try {
//            blankBitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(), CameraActivity.IMG_URI);
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(CameraActivity.IMG_File));
//            Bitmap bitmap =MediaStore.Images.Media.getBitmap(getContentResolver(),CameraActivity.IMG_URI);
                } catch (IOException e) {
                    Log.e("ERROR","Bitmap读取错误！");
                    e.printStackTrace();
                }
                double[] bitmapDoubleValue=new double[bitmap.getHeight()*bitmap.getWidth()];
                bitmapDoubleValue=convertGreyImg(bitmap);
//            image.setImageBitmap(blankBitmap);
                int[] labeledBlankBitmap=segPicture(bitmapDoubleValue,bitmap.getWidth(),bitmap.getHeight());
                testPointValue[ind] = getValue(bitmapDoubleValue, labeledBlankBitmap, 1,bitmap.getWidth(),bitmap.getHeight());
                testPointValue_normalized[ind] = (blankValue - testPointValue[ind]) / blankValue;

                if (ind < testPointNum - 1)
                    getPoints(ind + 1);
                else {
                    //Debug
//                for(int ii=0;ii<testPointNum;ii++)
//                {
//                    System.out.print(testPointConcentration[ii]);
//                    System.out.print(" ");
//                }
//                System.out.println();
//                for(int ii=0;ii<testPointNum;ii++)
//                {
//                    System.out.print(testPointValue[ii]);
//                    System.out.print(" ");
//                }
//                System.out.println();

                    fitting();
                    draw();
                    CameraActivity.IMG_File.delete();
                }
            }else{
                Intent intent=new Intent(DrawingActivity.this,TestActivity.class);
                intent.putExtra(TestActivity.ExtraType,TestActivity.NEW);
                intent.putExtra("IMG_URL",CameraActivity.IMG_URI.toString());
                intent.putExtra("slope",String.valueOf(slope));
                intent.putExtra("intercept",String.valueOf(intercept));
                intent.putExtra("blank",String.valueOf(blankValue));
                intent.putExtra("chartName",name);
//                intent.putExtra(DrawingActivity.ExtraType,DrawingActivity.LOAD);
                intent.putExtra("R_squared",String.valueOf(R_squared));
                intent.putExtra("pointNum",String.valueOf(testPointNum));
                intent.putExtra("time",dataMap.get("time"));
                for(int i=0;i<testPointNum;i++) {
                    intent.putExtra("pointConcentration"+i,String.valueOf(testPointConcentration[i]));
                    intent.putExtra("pointValue"+i,String.valueOf(testPointValue_normalized[i]));
                }

                startActivity(intent);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void draw(){
        List<ILineDataSet> sets = new ArrayList<>();  // 多条线
        List<Entry> list=new ArrayList<>();
        for(int i=0;i<testPointNum;i++) {
            list.add(new Entry((float) testPointConcentration[i], (float)testPointValue_normalized[i]));     //其中两个数字对应的分别是   X轴   Y轴
        }
        List<Entry> straight =new ArrayList<>();
        straight.add(new Entry(0,(float) intercept));
        double Xmax = Arrays.stream(testPointConcentration).max().getAsDouble();;
        straight.add(new Entry((float)(Xmax*1.1),(float)(slope*(Xmax*1.1)+intercept)));
        LineDataSet lineDataSet=new LineDataSet(list,"标准点");
        LineDataSet straightDataSet=new LineDataSet(straight,"拟合曲线");
        sets.add(lineDataSet);
        sets.add(straightDataSet);
        LineData lineData=new LineData(sets);
        line.setData(lineData);

        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setColor(Color.parseColor("#00FFFFFF"));
        lineDataSet.setCircleColor(Color.parseColor("#FF000000"));
//        lineDataSet.setDrawCircles(false);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setCircleRadius(4);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
//        lineDataSet.setCircleHoleRadius(3);
        lineDataSet.setHighlightLineWidth(2);

        straightDataSet.setColor(Color.parseColor("#FF990000"));
        straightDataSet.setDrawCircles(false);
        straightDataSet.setDrawCircleHole(false);
        straightDataSet.setHighlightLineWidth(2);
        straightDataSet.setLineWidth(3);
        straightDataSet.setDrawValues(false);
        straightDataSet.setDrawHighlightIndicators(false);

        slope_textView.setText("Slope: "+String.format("%.10f", slope));
        intercept_textView.setText("Intercept: "+String.format("%.10f",intercept));
        R_squared_textView.setText("R-squared: "+String.format("%.10f",R_squared));
        pointNum_textView.setText("N: "+String.valueOf(testPointNum));

        if(type==NEW){
            dataMap.put(ChartListItem.SLOPE,String.valueOf(slope));
            dataMap.put(ChartListItem.INTERCEPT,String.valueOf(intercept));
            dataMap.put(ChartListItem.R_SQUARED,String.valueOf(R_squared));
            dataMap.put(ChartListItem.POINTNUM,String.valueOf(testPointNum));
            String time = new SimpleDateFormat("yyyy.MM.dd\nHH:mm:ss", Locale.CHINA).format(new Date());
            dataMap.put(ChartListItem.TIME,time);
            dataMap.put("blank",String.valueOf(blankValue));
            for(int i=0;i<testPointNum;i++) {
                dataMap.put("pointConcentration"+i,String.valueOf(testPointConcentration[i]));
                dataMap.put("pointValue"+i,String.valueOf(testPointValue_normalized[i]));
            }
        }
    }

    /**
     * 拟合曲线
     */
    public void fitting(){
        double Xmean=0;
        double Ymean=0;
        double XY=0;
        double X2=0;
        for(int i=0;i<testPointNum;i++)
        {
            XY+=testPointConcentration[i]*testPointValue_normalized[i];
            X2+=testPointConcentration[i]*testPointConcentration[i];
            Xmean+=testPointConcentration[i];
            Ymean+=testPointValue_normalized[i];
        }
        Xmean/=(double)testPointNum;
        Ymean/=(double)testPointNum;

        slope=(XY-testPointNum*Xmean*Ymean)/(X2-testPointNum*Xmean*Xmean);
        intercept=Ymean-slope*Xmean;
        double R_numerator=0;
        double R_denominator=0;
        for(int i=0;i<testPointNum;i++)
        {
            double prediction=slope*testPointConcentration[i]+intercept;
            R_numerator+=(prediction-Ymean)*(prediction-Ymean);
            R_denominator+=(testPointValue_normalized[i]-Ymean)*(testPointValue_normalized[i]-Ymean);
        }
        R_squared=R_numerator/R_denominator;
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
            File dir = new File(getExternalFilesDir(""),"Charts");
            if( !dir.exists()){
                dir.mkdirs();}
            File file = new File(dir,name);
            if(!file.exists()){
                file.createNewFile();
            }
            fileOutputStream = new FileOutputStream(file);
            oos=new ObjectOutputStream(fileOutputStream);
            oos.writeObject(dataMap);
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

    /**
     * 自定义图表的MarkerView(点击坐标点，弹出提示框)
     */
    class CustomMarkerView extends MarkerView {

        private TextView tvContent;
        /**
         *
         * @param context
         *            上下文
         * @param layoutResource
         *            资源文件
         */
        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            // 显示布局中的文本框
            tvContent = (TextView) findViewById(R.id.txt_tips);
        }

        // 每次markerview回调重绘，可以用来更新内容
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            // 设置Y周数据源对象Entry的value值为显示的文本内容
            tvContent.setText(" X:"+e.getX()+" \n"+" Y:"+ e.getY()+" ");
        }

//        @Override
//        public int getXOffset(float xpos) {
//            // 水平居中
//            return -(getWidth() / 2);
//        }
//
//        @Override
//        public int getYOffset(float ypos) {
//            // 提示框在坐标点上方显示
//            return -getHeight();
//        }

        private MPPointF mOffset;
        @Override
        public MPPointF getOffset() {
            if(mOffset == null) {
                // center the marker horizontally and vertically
                mOffset = new MPPointF(-(getWidth() / 2), -getHeight());
            }
            return mOffset;
        }
    }

}

