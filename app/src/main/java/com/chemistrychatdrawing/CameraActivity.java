package com.chemistrychatdrawing;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ERP_LXKUN_JAK on 2020/10/22
 * Current project HaoRuiDoctor
 * Annotations ：相机相册裁剪图片操作
 */

public class CameraActivity extends AppCompatActivity {

    public static String TAG = "CameraActivity:";
    public static final String ExtraType = "CameraExtra";   //Activity传值key
    public static final int CAMERA = 99001;
    public static final int PHOTO = 99002;
    public static int pointNum = 0;

    public static Uri IMG_URI = null;
    public static File IMG_File = null;
    public static boolean LISTENING = false;
    public static Bitmap IMG_Bitmap=null;


    private static int aspectX = 0;  //设置裁剪区域的宽高比例
    private static int aspectY = 0;  //设置裁剪区域的宽高比例
    private static int outputX = 300; //设置裁剪区域的宽度和高度
    private static int outputY = 300; //设置裁剪区域的宽度和高度
    private static boolean scale = true;      //裁剪时是否可以缩放
    private static boolean noFaceDetection = false;  //是否检测人脸
    private static String outputFormat = Bitmap.CompressFormat.JPEG.toString();  //图片输出格式

    //设置图片输出格式
    public static void setOutputFormat(String outputFormats) {
        outputFormat = outputFormats;
    }

    //设置裁剪比例
    public static void setClipRatio(int aspectXs, int aspectYs) {
        aspectX = aspectXs;
        aspectY = aspectYs;
    }

    //设置裁剪像素
    public static void setClipPixel(int outputXs, int outputYs) {
        outputX = outputXs;
        outputY = outputYs;
    }

    //裁剪时是否可以缩放
    public static void setScales(boolean scales) {
        scale = scales;
    }

    //是否检测人脸
    public static void setNoFaceDetections(boolean noFaceDetections) {
        noFaceDetection = noFaceDetections;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int CameraExtra = intent.getIntExtra(ExtraType, 0);
        pointNum=intent.getExtras().getInt("pointIndex");

        //动态获取权限
        boolean WRITE2 = RequestPermissions(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        boolean READ2 = RequestPermissions(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean CAMERA2 = RequestPermissions(CameraActivity.this, Manifest.permission.CAMERA);

        requestmanageexternalstorage_Permission();

        if (WRITE2 && READ2 && CAMERA2) {
            switch (CameraExtra) {
                case CAMERA:
                    openCamera();  //相机操作
                    break;
                case PHOTO:
                    openGallery();  //相册操作
                    break;
            }
        } else {
            CameraActivity.this.finish();
        }
    }

    private void requestmanageexternalstorage_Permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                Log.e("TAG","Android VERSION  R OR ABOVE，HAVE MANAGE_EXTERNAL_STORAGE GRANTED!");
            } else {
                Log.e("TAG","Android VERSION  R OR ABOVE，NO MANAGE_EXTERNAL_STORAGE GRANTED!");
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, 3);
            }
        }
    }

    // 申请相机权限的requestCode
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 10;
    private static final int PERMISSION_ALBUM_REQUEST_CODE = 11;
    private static final int REQUEST_TAKE_PHOTO = 0;// 拍照
    private static final int REQUEST_CROP = 1;// 裁剪
    private static final int SCAN_OPEN_PHONE = 2;// 相册
    private Uri mCameraUri;//拍照时返回的uri
    private String mCameraImagePath;// 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    private boolean isAndroidQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;// 是否是Android 10以上手机
    private Uri mCutUri;// 图片裁剪时返回的uri
    private File imgFile;// 拍照保存的图片文件
    private File mCutFile;

    public static String DIRECTORY_ALARMS = "Alarms";
    public static String DIRECTORY_AUDIOBOOKS = "Audiobooks";
    public static String DIRECTORY_DCIM = "DCIM";
    public static String DIRECTORY_DOCUMENTS = "Documents";
    public static String DIRECTORY_DOWNLOADS = "Download";
    public static String DIRECTORY_MOVIES = "Movies";
    public static String DIRECTORY_MUSIC = "Music";
    public static String DIRECTORY_NOTIFICATIONS = "Notifications";
    public static String DIRECTORY_PICTURES = "Pictures";
    public static String DIRECTORY_PODCASTS = "Podcasts";

    /**
     * 从相册获取图片
     */
    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        photoPickerIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        photoPickerIntent.setType("image/*");
        //   photoPickerIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(photoPickerIntent, SCAN_OPEN_PHONE);
    }

    /**
     * 处理权限申请的回调。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //允许权限，有调起相机拍照。
                openCamera();
            } else {
                //拒绝权限，弹出提示框。
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * 裁剪图片
     */
    private void cropPhoto1(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        // Intent intent = new Intent("android.intent.action.EDIT");
        // intent.setAction("android.intent.action.EDIT");
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(uri, "image/*");

        //  intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("return-data", false);
        File cropTemp = this.getExternalFilesDir(DIRECTORY_DCIM);
        File cropTempName = new File(cropTemp, System.currentTimeMillis() + "_crop_temp.png");
        Log.e("getPath", cropTempName.getAbsolutePath());

        Uri uriForFile = FileProvider.getUriForFile(this, "com.chemistrychatdrawing.fileprovider", cropTempName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);

        grantPermissionFix(intent, uriForFile);
        grantPermissionFix(intent, uri);

        startActivityForResult(intent, REQUEST_CROP);

    }

    private void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 0);
        intent.putExtra("aspectY", 0);
//        intent.putExtra("outputX", outputX);
//        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);

        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
        String fileName = time;   //文件命名
        mCutFile = new File(Environment.getExternalStorageDirectory()+"/ChemistryApp", fileName + ".jpg");
        if (!mCutFile.getParentFile().exists()) {
            mCutFile.getParentFile().mkdirs();
        }
        mCutUri = Uri.fromFile(mCutFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
//        Toast.makeText(this, "剪裁图片", Toast.LENGTH_SHORT).show();
//         以广播方式刷新系统相册，以便能够在相册中找到刚刚所拍摄和裁剪的照片
        Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentBc.setData(uri);
        this.sendBroadcast(intentBc);
        startActivityForResult(intent,REQUEST_CROP);
    }

    /**
     * 图片剪裁
     *
     * @param uri 图片uri
     */
    private void cropPhoto3(Uri uri) {
        // 调用系统中自带的图片剪裁
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        // 返回裁剪后的数据
        intent.putExtra("return-data", true);

//        String time = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date());
//        String fileName = time;   //文件命名
//        mCutFile = new File(Environment.getExternalStorageDirectory() + "/take_photo/", fileName + ".jpg");
//        if (!mCutFile.getParentFile().exists()) {
//            mCutFile.getParentFile().mkdirs();
//        }
//        mCutUri = Uri.fromFile(mCutFile);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
        startActivityForResult(intent, REQUEST_CROP);
    }

    private void grantPermissionFix(Intent intent, Uri uri) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setAction(null);
            intent.setComponent(new ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
            break;
        }
    }


    private void openCamera() {
        //用于保存调用相机拍照后所生成的文件
        imgFile = new File(this.getExternalFilesDir(DIRECTORY_DCIM), System.currentTimeMillis() + ".png");
        //跳转到调用系统相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //判断版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //如果在Android7.0以上,使用FileProvider获取Uri
            intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(CameraActivity.this, "com.chemistrychatdrawing.fileprovider", imgFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);

        } else {    //否则使用Uri.fromFile(file)方法获取Uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));
        }
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.e("d", "--------------222222222-------requestCode--: " + requestCode);
        switch (requestCode) {

            case REQUEST_TAKE_PHOTO:   //调用相机后返回
                if (resultCode == RESULT_OK) {
                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri contentUri = FileProvider.getUriForFile(CameraActivity.this, "com.chemistrychatdrawing.fileprovider", imgFile);
                        cropPhoto(contentUri);
                    } else {
                        cropPhoto(Uri.fromFile(imgFile));
                    }
                }
                break;
            case SCAN_OPEN_PHONE:    //调用相册后返回
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    //mHeader_iv.setImageURI(uri);
                    if (null != uri)
                        cropPhoto(uri);
                    else {
                        Log.e("e", "null");
                    }
                }
                break;
            case REQUEST_CROP:     //调用剪裁后返回
                Log.e("d", "--------------222222222-------");
                if (null != intent) {
                    Log.e("d", "---------------------not null");

                    CameraActivity.LISTENING = true;
//                    //图片剪裁返回
//                    Bundle bundle = intent.getExtras();
//                    if (bundle != null) {
//                        //在这里获得了剪裁后的Bitmap对象，可以用于上传
//                        IMG_Bitmap = bundle.getParcelable("data");
//                    }

//                    Uri data = intent.getData();
//                    IMG_URI=data;
                    IMG_URI=mCutUri;
                    IMG_File = new File(IMG_URI.getPath());
//                    try {
//                        IMG_Bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(), IMG_URI);
//                    } catch (IOException e) {
//                        Log.e("ERROR","CamareBitmap保存失败！");
//                        e.printStackTrace();
//                    }

//                    Bundle bundle = intent.getExtras();
//                    if (bundle != null) {
//                        //在这里获得了剪裁后的Bitmap对象，可以用于上传
//                        Bitmap image = bundle.getParcelable("data");

//                        //也可以进行一些保存、压缩等操作后上传
////                    String path = saveImage("crop", image);
//                    }
                    CameraActivity.this.finish();
                } else {
                    Log.e("d", "---------null");
                }
                break;
            default:
                CameraActivity.this.finish();
        }
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * 创建保存图片的文件
     */
    private File createImageFile() throws IOException {
        String imageName = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageName + ".jpg");
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public File uriToFileApiQ(Uri uri) {
        File file = null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = CameraActivity.this.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try {
                    InputStream is = contentResolver.openInputStream(uri);
                    File cache = new File(CameraActivity.this.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
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
     * 动态申请权限
     *
     * @param context    上下文
     * @param permission 要申请的一个权限，列如写的权限：Manifest.permission.WRITE_EXTERNAL_STORAGE
     * @return 是否有当前权限
     */
    private boolean RequestPermissions(@NonNull Context context, @NonNull String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            Log.i("requestMyPermissions", ": 【 " + permission + " 】没有授权，申请权限");
            ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, 100);
            return false;
        } else {
            Log.i("requestMyPermissions", ": 【 " + permission + " 】有权限");
            return true;
        }
    }

    /**
     * user转换为file文件
     * 返回值为file类型
     *
     * @param uri
     * @return
     */
    private File uri2File(Activity activity, Uri uri) {
        String img_path;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor actualimagecursor = activity.managedQuery(uri, proj, null, null, null);
        if (actualimagecursor == null) {
            img_path = uri.getPath();
        } else {
            int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualimagecursor.moveToFirst();
            img_path = actualimagecursor.getString(actual_image_column_index);
        }
        File file = new File(img_path + ".jpg");
        return file;
    }

}

