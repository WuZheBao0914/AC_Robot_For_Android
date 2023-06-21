package cn.edu.uestc.cssl.util;

import android.content.Context;
import android.graphics.Bitmap;

import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtils {
    /**
     * Save Bitmap
     *
     * @param name file name
     * @param bm  picture to save
     */
    public static File saveBitmap(String name,String dirname,Bitmap bm, Context mContext) {

        Log.i("Save Bitmap", "Ready to save picture");
        //指定我们想要存储文件的地址
        String TargetPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + dirname;
        File saveFile = new File(TargetPath, name);
        File targetpath = new File(TargetPath);
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        //判断指定文件夹的路径是否存在
        if (!targetpath.exists()) {
            targetpath.mkdir();
            Log.i("Save Bitmap", "TargetPath isn't exist");
        }
        try {
            FileOutputStream saveImgOut = new FileOutputStream(saveFile);
            // compress - 压缩的意思
            bm.compress(Bitmap.CompressFormat.JPEG, 100, saveImgOut);
            //存储完成后需要清除相关的进程
            saveImgOut.flush();
            saveImgOut.close();
            Log.i("Save Bitmap", "The picture is save to your phone!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return saveFile;
    }
}
