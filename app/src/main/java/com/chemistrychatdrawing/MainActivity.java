package com.chemistrychatdrawing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity   implements BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    ViewPager viewPager;
    BottomNavigationView mNavigationView;
    ChartFragment chartFragments = new ChartFragment();
    TestFragment  testFragments = new TestFragment();
    View contentView;
    PopupWindow popupWindow;
    Uri uri;
    int pointNumforNewChat=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //页面初始化导航栏
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setTitleTextAppearance(this, R.style.Toolbar_TitleText);

        //设置裁剪比例
        CameraActivity.setClipRatio(0, 0);

        // setOutputFormat()  设置图片输出格式
        // setClipRatio()  设置裁剪比例
        // setClipPixel()  设置裁剪像素
        // setScales()  裁剪时是否可以缩放
        // setNoFaceDetections()  是否检测人脸
        getChartFile();
        getTestFile();
        init();
    }

    private void init() {

        //获取页面标签对象
        viewPager = findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(this);
        mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setOnNavigationItemSelectedListener(this);

        initPopupWindow();
        //页面切换
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0:
                        return chartFragments;
                    case 1:
                        return  testFragments;
                }

                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
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

    public void getChartFile() {
        FileInputStream fileInputStream = null;
        ObjectInputStream ois=null;
        File dir = new File(getExternalFilesDir(""), "Charts");
        File[] files = dir.listFiles();
        ChartListItem.clear();
        if(files!=null&&files.length>0) {
            for (File f : files) {                    //遍历File[]数组
                if (!f.isDirectory())        //若非目录(即文件)
                {
                    try {
                        fileInputStream = new FileInputStream(f);
                        ois = new ObjectInputStream(fileInputStream);
                        Map<String, String> maps = (HashMap<String, String>) ois.readObject();
                        int num=Integer.parseInt(maps.get(ChartListItem.POINTNUM));
                        String[] testPointConcentration =new String[num];
                        String[] testPointValue_normalized =new String[num];
                        for(int i=0;i<num;i++) {
                            testPointConcentration[i]=maps.get("pointConcentration"+i);
                            testPointValue_normalized[i]=maps.get("pointValue"+i);
                        }
                        ChartListItem.addItem(maps.get(ChartListItem.TIME),
                                maps.get(ChartListItem.CHARTNAME),
                                maps.get(ChartListItem.SLOPE),
                                maps.get(ChartListItem.INTERCEPT),
                                maps.get(ChartListItem.R_SQUARED),
                                maps.get(ChartListItem.POINTNUM),
                                maps.get("blank"),
                                testPointConcentration,
                                testPointValue_normalized);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (ois != null) {
                            try {
                                ois.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public void getTestFile() {
        FileInputStream testFileInputStream = null;
        ObjectInputStream testois=null;
        FileInputStream chartFileInputStream = null;
        ObjectInputStream chartois=null;
        File testDir = new File(getExternalFilesDir(""), "Tests");
        File[] testFiles = testDir.listFiles();
        TestListItem.clear();
        if(testFiles!=null&&testFiles.length>0) {
            for (File testf : testFiles) {                    //遍历File[]数组
                if (!testf.isDirectory())        //若非目录(即文件)
                {
                    try {
                        testFileInputStream = new FileInputStream(testf);
                        testois = new ObjectInputStream(testFileInputStream);
                        TestActivity.TestData testData = (TestActivity.TestData) testois.readObject();
                        String chartName=testData.chartName;
                        File chartDir = new File(getExternalFilesDir(""), "Charts");
                        File chartFile=new File(chartDir,chartName);
                        chartFileInputStream = new FileInputStream(chartFile);
                        chartois = new ObjectInputStream(chartFileInputStream);

                        //加载对应Chart数据
                        Map<String, String> maps = (HashMap<String, String>) chartois.readObject();
                        int num=Integer.parseInt(maps.get(ChartListItem.POINTNUM));
                        String[] testPointConcentration =new String[num];
                        String[] testPointValue_normalized =new String[num];
                        for(int i=0;i<num;i++) {
                            testPointConcentration[i]=maps.get("pointConcentration"+i);
                            testPointValue_normalized[i]=maps.get("pointValue"+i);
                        }
                        ChartListItem.CItem cItem=new ChartListItem.CItem(maps.get(ChartListItem.TIME),
                                maps.get(ChartListItem.CHARTNAME),
                                maps.get(ChartListItem.SLOPE),
                                maps.get(ChartListItem.INTERCEPT),
                                maps.get(ChartListItem.R_SQUARED),
                                maps.get(ChartListItem.POINTNUM),
                                maps.get("blank"),
                                testPointConcentration,
                                testPointValue_normalized);

                        //加载Test数据
                        TestListItem.addItem(cItem,testData.testName,testData.bitmapBytes,testData.values);

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (testois != null) {
                            try {
                                testois.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (testFileInputStream != null) {
                            try {
                                testFileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (chartois != null) {
                            try {
                                chartois.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (chartFileInputStream != null) {
                            try {
                                chartFileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    private void showPopWindow() {
        View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
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
                startActivity(new Intent(MainActivity.this, CameraActivity.class).putExtra(CameraActivity.ExtraType, CameraActivity.CAMERA));
                popupWindow.dismiss();
            }
        });
        album_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class).putExtra(CameraActivity.ExtraType, CameraActivity.PHOTO));
                popupWindow.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获得相册、相机返回的结果，并显示
        if (CameraActivity.LISTENING) {
            Log.e("TAG", "返回的Uri结果：" + CameraActivity.IMG_URI);
            Log.e("TAG", "返回的File结果：" + CameraActivity.IMG_File.getPath());
            Log.e("TAG", "返回的Bitmap结果：" + CameraActivity.IMG_Bitmap);
            CameraActivity.LISTENING = false;   //关闭获取结果
//            IMG.setImageURI(CameraActivity.IMG_URI);  //显示图片到控件
            Intent intent=new Intent(MainActivity.this,DrawingActivity.class);
            intent.putExtra("IMG_URL",CameraActivity.IMG_URI.toString());
            intent.putExtra("point_num",pointNumforNewChat);
            intent.putExtra(DrawingActivity.ExtraType,DrawingActivity.NEW);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User chose the "Settings" item, show the app settings UI...
        final EditText editText = new EditText(MainActivity.this);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle("请输入标准点数量");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        dialogBuilder.setView(editText);

        dialogBuilder.setCancelable(false);
        dialogBuilder.setPositiveButton("开始确认灰度", null);
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog=dialogBuilder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,editText.getText(),Toast.LENGTH_SHORT).show();
                try {
                    pointNumforNewChat = Integer.parseInt(editText.getText().toString());
                    if(pointNumforNewChat==0) {
                        Toast.makeText(MainActivity.this,"不能为0！",Toast.LENGTH_LONG).show();
                    }else {
                        showPopWindow();
                        dialog.dismiss();
                    }
                }catch (Exception e)
                {
                    Log.e("Error",e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this,"非法输入！",Toast.LENGTH_LONG).show();
                }

//                File outImage=new File(getExternalCacheDir(),"output_image.jpg");
//                try{
//                    if(outImage.exists())
//                    {
//                        outImage.delete();
//                    }
//                    outImage.createNewFile();
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//                if(Build.VERSION.SDK_INT>=24)
//                {
//                    uri= FileProvider.getUriForFile(MainActivity.this,"com.example.gdzc.cameraalbumtest.fileprovider",outImage);
//                }
//                else
//                {
//                    uri= Uri.fromFile(outImage);
//                }
//                Intent intent=new Intent("android.media.action.IMAGE_CAPTURE");
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
//                startActivityForResult(intent,1);
            }
        });

        return super.onOptionsItemSelected(item);
    }




    //实现接口的相关方法  implements上面两个方法后 alt+enter就会弹出这些接口，直接回车实现他们
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mNavigationView.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        viewPager.setCurrentItem(menuItem.getOrder());
        return true;
    }
}