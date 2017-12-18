package com.fishpool.stylecreator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import static com.fishpool.stylecreator.LoginActivity.*;

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

    /**
     * 检查用户是否登录
     * @return 登录返回true
     */
    public static boolean checkLogin(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(TagAlreadySignIn,false);
    }

    public static boolean signIn(String email,String password,Handler handler){
        //TODO 还需要实现登录操作
        Message message = Message.obtain();
        message.what = MessageTypes.SignIn;
        message.obj = Strings.SignInSucessfully;
        handler.sendMessage(message);
        return true;
    }

    public static boolean signUp(String email,String password,String confirmPassword,Handler handler){
        //TODO 还需要实现注册操作
        Message message = Message.obtain();
        message.what = MessageTypes.SignUp;
        message.obj = Strings.SignUpSucessfully;
        handler.sendMessage(message);
        return true;
    }
}
