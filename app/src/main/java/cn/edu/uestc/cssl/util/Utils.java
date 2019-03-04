package cn.edu.uestc.cssl.util;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

    /**
     * Reads a text file with the specified resource id.
     *
     * @param context The parent Context
     * @param resId   The resource id of the text file
     */
    public static String readText(Context context, int resId) {

        InputStream inputStream = context.getResources().openRawResource(resId);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return byteArrayOutputStream.toString();
    }
}
