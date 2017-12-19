package com.fishpool.stylecreator;

/**
 * 全局常量定义
 */

public class ConstValues {
    //调试标志
    public final static String TAG = "StyleCreator";

    public static class Strings{
        public final static String SignIn = "登录";
        public final static String SignUp = "注册";
        public final static String SignOut = "注销";
        public final static String EditPassword = "修改密码";
        public final static String PersonalInfo = "个人信息";
        public final static String SignInSucessfully = "登录成功";
        public final static String SignInFailed = "登录失败";
        public final static String SignUpSucessfully = "注册成功";
        public final static String SignUpFailed = "登录失败";
        public final static String OperationCanceled = "操作取消";
        public final static String EmailIsEmpty = "邮箱为空";
        public final static String PasswordIsEmpty = "密码为空";
        public final static String ComfirmPasswordIsEmpty = "确认密码为空";
        public final static String PasswordNotEqualWithConfirmPassword = "两次密码不同";
    }

    //消息类型
    public static class MessageTypes{
        public final static int BitmapDownloadFinished = 1;
        public final static int TimeIsUp = 2;
        public final static int SignIn = 3;
        public final static int SignUp = 4;
    }
    //请求类型
    public static class RequestCodes{
        //请求Activity
        public final static int Result = 0;
        public final static int RequestCrop = 1;
        public final static int RequestChooseOnePicture = 2;
        public final static int RequestSignIn = 3;
        public final static int RequestSignUp = 4;

        //请求权限
        public final static int RequestWritePermission = 22;
        public final static int RequestReadPermission = 23;
    }
}
