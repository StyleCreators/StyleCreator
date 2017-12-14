package com.fishpool.stylecreator;

/**
 * 全局常量定义
 */

public class ConstValues {
    //调试标志
    public final static String TAG = "StyleCreator";

    //消息类型
    public static class MessageTypes{
        public static final int BitmapDownloadFinished = 1;
        public static final int TimeIsUp = 2;
    }
    //请求类型
    public static class RequestCodes{
        //请求Activity
        public final static int Result = 0;
        public final static int RequestCrop = 1;
        public final static int RequestChooseOnePicture = 0;

        //请求权限
        public final static int RequestWritePermission = 22;
        public final static int RequestReadPermission = 23;
    }
}
