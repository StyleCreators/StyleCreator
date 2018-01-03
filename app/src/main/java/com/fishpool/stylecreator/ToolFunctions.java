package com.fishpool.stylecreator;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static Bitmap getLocalBitmap(String url) {
        if(url==null){
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Tool.E0052: "+e.toString());
            return null;
        }
    }
    public static Bitmap getLocalSmallBitmap(String path) {
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
                Log.d(TAG, "getLocalSmallBitmap: "+option.inSampleSize);
            }
            Bitmap  bitmap= BitmapFactory.decodeFile(path,option);
            return bitmap;
        } catch (Exception e) {
            Log.d(TAG, "Tool.E0077: "+e.toString());
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
                m.what = MsgTypes.BitmapDownloadFinished;
                m.arg1 = getIdFromUrl(url);
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
    public static Bitmap getHttpBitmap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            Log.d(TAG, "getHttpBitmap:"+url);
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Tool.E0113: "+e.toString());
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
            Log.d(TAG, "Tool.E0124: "+e.toString());;
        }catch (IOException e){
            Log.d(TAG, "Tool.E0126: "+e.toString());
        }
        return bitmap;
    }

    /**
     * 检查用户是否登录
     * @return 登录返回true
     */
    public static boolean checkLogin(Context context){
        if(context==null){
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(TagAlreadySignIn,false);
    }
    public static boolean signIn(Context context,Handler handler,String email,String password){
        //TODO 还需要实现登录操作
        Message message = Message.obtain();
        message.what = MsgTypes.SignIn;
        message.obj = Strings.SignInSucessfully;

        SharedPreferences sp = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        Log.d(TAG, "signIn: "+sp.getString(TagEmail,""));
        if(!email.equals(sp.getString(TagEmail,""))){
            Log.d(TAG, "signIn: ");
            message.obj = Strings.UserNotExist;
        }else if(!password.equals(sp.getString(TagPassword,""))){
            message.obj = Strings.PasswordNotCorrect;
        }

        handler.sendMessage(message);
        return true;
    }
    public static boolean signUp(Context context,Handler handler,String email,String password,
                                 String confirmPassword){
        //TODO 还需要实现注册操作
        SharedPreferences sp = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(TagEmail,email);
        editor.putString(TagPassword,password);
        editor.apply();
        Message message = Message.obtain();
        message.what = MsgTypes.SignUp;
        message.obj = Strings.SignUpSucessfully;
        handler.sendMessage(message);
        return true;
    }
    public static HashMap<String,String> getUserInfo(Context context){
        HashMap<String,String> infoes = new HashMap<>();
        SharedPreferences sp = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        infoes.put("name","姓名:"+sp.getString(LoginActivity.TagEmail,"error"));
        infoes.put("email","邮箱:"+sp.getString(LoginActivity.TagEmail,"error"));
        //infoes.put("image_count","照片数量:"+sp.getString("image_count","-1"));
        return infoes;
    }
    public static boolean signOut(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(TagAlreadySignIn,false);
        editor.apply();
        return true;
    }

    public static boolean uploadOriginImage(final Handler handler, final String email,
                                            final String filePath,final int styleType){
        String[] strings = filePath.split("/");
        final String fileName = strings[strings.length-1];
        final String strUrl = "http://10.0.2.2/post.php?uid="+email+"&filename="+fileName+"&styletype="+styleType;
        Log.d(TAG, "uploadOriginImage: "+strUrl);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url=new URL(strUrl);
                    HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("content-type", "text/html");

                    //读取文件上传到服务器
                    BufferedOutputStream out=new BufferedOutputStream(connection.getOutputStream());
                    File file=new File(filePath);
                    FileInputStream fileInputStream=new FileInputStream(file);
                    byte[]bytes=new byte[1024];
                    int numReadByte=0;
                    while((numReadByte=fileInputStream.read(bytes,0,1024))>0)
                    {
                        out.write(bytes, 0, numReadByte);
                    }
                    out.flush();
                    fileInputStream.close();

                    //读取URLConnection的响应
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    String result="";
                    while ((line = in.readLine()) != null) {
                        result += line;
                    }

                    //将结果返回给主线程
                    Message msg = Message.obtain();
                    msg.what = MsgTypes.ServerProcessFinished;
                    msg.obj = result;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    Log.d(TAG, "Tool.E0197: "+e.toString());
                    //将结果返回给主线程
                    Message msg = Message.obtain();
                    msg.what = MsgTypes.ServerProcessFinished;
                    msg.obj = "Error:上传图片失败";
                    handler.sendMessage(msg);
                }
            }
        }).start();
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

    public static long getCRC32(@NonNull File file) {
        if(file==null){
            return -1;
        }
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
        String style = getStyledPathPrefix()+id;
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
    public static Bitmap addWhiteBarForBitmap(Bitmap bitmap) {
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

    private static int getIdFromUrl(String url){
        String[] list = url.split("/");
        try {
            String idStr = list[list.length-1].replace(".jpeg","");
            return Integer.parseInt(idStr);
        }catch (Exception e){
            return 0;
        }
    }
    public static String getOriginPathByUrl(String url){
        int id = getIdFromUrl(url);
        if (id==0){
            return null;
        }
        return getOriginPathPrefix()+id;
    }
    public static String getStylePathByUrl(String url){
        int id = getIdFromUrl(url);
        if (id==0){
            return null;
        }
        return getStyledPathPrefix()+id;
    }
    public static String getStylePathById(int id){
        if (id==0){
            return null;
        }
        return getStyledPathPrefix()+id;
    }
    public static void saveBitmapToPath(Bitmap bitmap,String path){
        if(path==null){
            return;
        }
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            if(bitmap.isRecycled()){
                bitmap.recycle();
            }
        } catch (IOException e) {
            Log.d(TAG, "Tool.E0148: "+e.toString());
        }
    }
}
