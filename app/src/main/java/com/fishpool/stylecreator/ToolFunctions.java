package com.fishpool.stylecreator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

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
    public static Bitmap getLoacalSmallBitmap(String path) {
        try {
            BitmapFactory.Options option = new BitmapFactory.Options();
            File file = new File(path);
            int maxSize = (int)(0.2*1024*1024);
            if(file.length()>maxSize){ //如果原图大于0.5M，压缩，避免程序崩溃和加快载入速度
                int inSampleSize = (int)(file.length()/maxSize);
                if(inSampleSize <= 0){
                    inSampleSize = 1;
                }
                option.inSampleSize = inSampleSize;
                Log.d(TAG, "getLoacalBitmap: "+option.inSampleSize);
            }
            Bitmap  bitmap= BitmapFactory.decodeFile(path,option);
            return bitmap;
        } catch (Exception e) {
            Log.d(TAG, "getLoacalBitmap: "+e.toString());
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
    public static boolean getStyledPicture(final Handler handler,final String path){
        //TODO 完成风格化函数
        return true;
    }
    /**
     * 生成菜单项
     * @return 返回菜单项的List
     */
    public static List<String> getMenuList(){

        List<String> data = new ArrayList<>();
        data.add(Strings.SignIn);
        data.add(Strings.SignUp);
        data.add(Strings.PersonalInfo);
        return data;
    }

    public static int[] getPictureIds(){
        int[] mImgIds =  new int[] {R.drawable.add, R.drawable.index0, R.drawable.index1,R.drawable.index2,
                R.drawable.index3, R.drawable.index4, R.drawable.index5,
                R.drawable.index6 };
        return mImgIds;
    }

    public static long getCRC32(File file) {
        CRC32 crc32 = new CRC32();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                crc32.update(buffer,0, length);
            }
            return crc32.getValue();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "getCRC32: "+e.toString());
            return -1;
        } catch (IOException e) {
            Log.d(TAG, "getCRC32: "+e.toString());
            return -1;
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                Log.d(TAG, "getCRC32: "+e.toString());
            }
        }
    }

    public static String getSavePathPrefix(){
        String path = Environment.getExternalStorageDirectory().getPath() + "/StyleCreator/";
        File f = new File(path);
        if(!f.exists())
            if(!f.mkdir())
                Log.d(TAG, "getSavePathPrefix: 创建文件夹失败");
        return path;
    }
    public static String getStyledPathPrefix(){
        String path = getSavePathPrefix() + "styled/";
        File f = new File(path);
        if(!f.exists())
            if(!f.mkdir())
                Log.d(TAG, "getSavePathPrefix: 创建文件夹失败");
        return path;
    }
    public static String getOriginPathPrefix(){
        String path = getSavePathPrefix() + "origin/";
        File f = new File(path);
        if(!f.exists())
            if(!f.mkdir())
                Log.d(TAG, "getSavePathPrefix: 创建文件夹失败");
        return path;
    }

    public static ArrayList<String> getOriginImagePaths(){
        ArrayList<String> filePaths = new ArrayList<>();
        File[] files = new File(getOriginPathPrefix()).listFiles();
        for (File f:files) {
//            Log.d(TAG, "getOriginImagePaths: "+f.getAbsolutePath());
            filePaths.add(f.getAbsolutePath());
        }
        return filePaths;
    }

    public static void removePicFromStorage(int id){
        String origin = getOriginPathPrefix()+id;
        File originF = new File(origin);
        if(originF.exists())
            if(!originF.delete())
                Log.d(TAG, "removePicFromStorage: Failed to delete file");
        String style = getOriginPathPrefix()+id;
        File styleF = new File(style);
        if(styleF.exists())
            if(!styleF.delete())
                Log.d(TAG, "removePicFromStorage: Failed to delete file");
    }

    /**
     * 设置bitmap四周白边
     * @param bitmap 原图
     * @return
     */
    public static Bitmap addWhiteBarForBitmap(Bitmap bitmap)
    {
        int width = bitmap.getWidth();
        int height =  bitmap.getHeight();
        int num = 14;
        int sizeW = width+num;
        int sizeH = height+num;
        // 背图
        Bitmap newBitmap = Bitmap.createBitmap(sizeW, sizeH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // 生成白色的
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap, num / 2, num / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        // 画正方形的
        canvas.drawRect(0, 0, sizeW, sizeH, paint);
        return newBitmap;
    }
}
