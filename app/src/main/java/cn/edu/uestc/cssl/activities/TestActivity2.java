package cn.edu.uestc.cssl.activities;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.edu.uestc.ac_core.app.AcRobot;


//一层小马甲，本质是帮忙调用系统相册返回URL
//原因是直接用fragment调系统相册拿图片会崩溃。原因可能是图片太大了。
//不知道还有没有存在的理由
public class TestActivity2 extends AppCompatActivity {
    private final int SELECT_PIC = 101;
    private final int REQUEST_CODE_TAKE_PICTURE = 102;
    private final int SELECT_CLIPPER_PIC = 103;
    private final int CROP_REQUEST_CODE = 104;
    private File temp_file;
    private File crop_temp_file;
    private boolean act_only_once;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);
        act_only_once = true;
    }

    @Override
    protected void onResume() {
        if(act_only_once){
            Intent intent = getIntent();
            String data = intent.getStringExtra("mode");
            if(data.equals("0")){
                take_Photo();
            }
            else if(data.equals("1")){
                goAlbums();
            }
            act_only_once = false;
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void goAlbums() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_PIC);
    }
    private void take_Photo(){
        Uri mCameraTempUri;
        try {
            temp_file = getTempFile();
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.DATA, temp_file.getAbsolutePath());
            mCameraTempUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (mCameraTempUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            }
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("TestActivity2----",String.valueOf(resultCode));
        switch (requestCode) {
            case SELECT_PIC:

                if(data == null){
                    Log.i("TestActivity2----","data == null");
                }
                else{
                    String image_Path = handleImageOnKitKat(data);
                    Intent origin_Image = new Intent(TestActivity2.this,RobotController.class);
                    origin_Image.putExtra("image_url",image_Path);
                    setResult(3, origin_Image);
                }
                finish();
                break;
            case REQUEST_CODE_TAKE_PICTURE:
                Log.i("TestActivity2----","REQUEST_CODE_TAKE_PICTURE");
                if (resultCode == RESULT_OK) {
                    //用相机返回的照片去调用剪裁也需要对Uri进行处理
                        Uri contentUri = FileProvider.getUriForFile(this,"ACRobot.provider"
                                , temp_file);
                        cropPhoto(contentUri);
                    } else {
                        finish();
                    }
                break;
            case CROP_REQUEST_CODE:     //调用剪裁后返回
                if (data == null) {
                    Log.i("TestActivity2----","data == null");
                    return;
                }
                temp_file.delete();
                Log.i("TestActivity2----","CROP_REQUEST_CODE");
                Intent camera_Image = new Intent(TestActivity2.this,RobotController.class);
                camera_Image.putExtra("image_url",crop_temp_file.getAbsolutePath());
                setResult(4, camera_Image);
                finish();
                break;
            case SELECT_CLIPPER_PIC:
                String image_Clippered_Path = handleImageOnKitKat(data);
                Intent clippered_Image = new Intent(TestActivity2.this,RobotController.class);
                clippered_Image.putExtra("image_url",image_Clippered_Path);
                setResult(4, clippered_Image);
                finish();
                //获取拍照后图片路径
            default:
                break;
        }
    }

    private java.lang.String handleImageOnKitKat(Intent data) {
        java.lang.String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            java.lang.String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                java.lang.String id = docId.split(":")[1];
                // 解析出数字格式的id
                java.lang.String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content: //downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        // 根据图片路径显示图片
        return imagePath;
    }
    private java.lang.String getImagePath(Uri uri, java.lang.String selection) {
        java.lang.String path = null;
        // 通过Uri和selection来获取真实的图片路径

        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    private File getTempFile(){
//        this.applicationContext.getExternalCacheDir()
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/faceimage/");
        if (!file.exists()) {
            file.mkdir();
        }
        File image_file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/faceimage/" + System.currentTimeMillis() + "tempfile.jpg");
        try {
            if (!file.exists()) {
                if(!image_file.createNewFile()){
                    Log.i("TestActivity2----","createNewFile failed");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TestActivity2----","return file");
        return image_file;
    }
    private void cropPhoto(Uri uri) {
        //在7.0以上系统裁剪完毕之后，会提示“无法保存经过裁剪的图片”
        //这是因为，我们在7.0以上跨文件传输uri时候，需要用FileProvider,但是这里需要用
        //Uri.fromFile(file)生成的，而不是使用FileProvider.getUriForFile
        //intent.putExtra("set-as-wallpaper",true); 默认是false,当你弄成true的时候，你就会发现打开不是裁剪的，而是设置为壁纸的操作。
        // intent.putExtra("return-data", true);下面就可以获取到该bitmap
        // if (data != null && data.getParcelableExtra("data") != null) {
        //                mStream = new ByteArrayOutputStream();
        //                mBitmap = data.getParcelableExtra("data");
        //                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, mStream);
        //                /**图片可以应用了*/
        //                /**接下来就是上传到服务器*/
        //                File files = creatFile(mBitmap);//变成文件
        //                ...后续根据需要来...
        //}
        Log.i("TestActivity2----","cropPhoto");
        crop_temp_file = getTempFile();
        Uri contentUri = Uri.fromFile(crop_temp_file);
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Android 7.0需要临时添加读取Url的权限， 添加此属性是为了解决：调用裁剪框时候提示：图片无法加载或者加载图片失败或者无法加载此图片
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");//发送裁剪信号，去掉也能进行裁剪
        intent.putExtra("scale", true);// 设置缩放
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边

        if(Build.MANUFACTURER.equals("HUAWEI"))
        {//华为特殊处理 不然会显示圆
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        }
        else
        {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        //上述两个属性控制裁剪框的缩放比例。
        //当用户用手拉伸裁剪框时候，裁剪框会按照上述比例缩放。
        intent.putExtra("outputX", 224);//属性控制裁剪完毕，保存的图片的大小格式。
        intent.putExtra("outputY", 224);//你按照1:1的比例来裁剪的，如果最后成像是800*400，那么按照2:1的样式保存，
        intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());//输出裁剪文件的格式
        intent.putExtra("return-data", true);//是否返回裁剪后图片的Bitmap
        Log.i("TestActivity2----","cropPhoto");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);//设置输出路径
        startActivityForResult(intent, CROP_REQUEST_CODE);
    }
}