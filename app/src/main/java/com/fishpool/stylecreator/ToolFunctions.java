package com.fishpool.stylecreator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.fishpool.stylecreator.ConstValues.*;

/**
 * 提供一些工具函数
 */

public class ToolFunctions {

    /**
     * 从本地读取文件
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从url下载图片
     * @param handler
     * @param url
     */
    public static void getBitmapFromUrl(final Handler handler,final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = getHttpBitmap(url);
                Message m = Message.obtain();
                m.what = MessageTypes.BitmapDownloadFinished;
                m.obj = bitmap;
                handler.sendMessage(m);
            }
        }).start();
    }

    /**
     * 从url下载图片
     * @param url
     * @return
     */
    private static Bitmap getHttpBitmap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            Log.d(TAG, url);
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.d(TAG, "getHttpBitmap: "+e.toString());
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (NullPointerException e) {
            Log.d(TAG, "getHttpBitmap: "+e.toString());;
        }catch (IOException e){
            Log.d(TAG, "getHttpBitmap: "+e.toString());
        }
        return bitmap;
    }
}
