package com.fishpool.stylecreator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.fishpool.stylecreator.ConstValues.*;
import static com.fishpool.stylecreator.LoginActivity.SIGN_IN_SUCCESSFULLY;

public class MainActivity extends AppCompatActivity {

    private boolean hasWriteStoragePermission;
    private boolean hasReadStoragePermission;

    private final static boolean isHeigerThanAnroidM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private ImageView m_imageView;
    private Button m_btChoosePicture;
    private ProgressBar m_loadingProgressbar;

    //滑动菜单相关
    private ConstraintLayout layout;
    private boolean isMenuDrawerLayoutOpened = false;
    private DrawerLayout menuDrawerLayout;
    private ListView menuListView;
    private List<String> menuList;
    private ArrayAdapter<String> menuArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_imageView = (ImageView) findViewById(R.id.imageView);
        m_btChoosePicture = (Button)findViewById(R.id.btUploadPicture);
        m_btChoosePicture.setOnClickListener(onClickListener);
        m_loadingProgressbar = (ProgressBar)findViewById(R.id.loadingProgressBar);
        m_loadingProgressbar.setVisibility(View.INVISIBLE);
        //创建滑动菜单
        createSlideMenu();
        //检查权限
        if(isHeigerThanAnroidM)
            checkStoragePermission();
        else{
            hasWriteStoragePermission = true;
            hasReadStoragePermission = true;
        }
        //检查是否登录
        if(ToolFunctions.checkLogin(getApplicationContext())){
            //启动登录界面
            //startActivityForResult(LoginActivity.createIntent(this,false),RequestCodes.RequestSignIn);
        }else {
            startActivityForResult(LoginActivity.createIntent(this, true),RequestCodes.RequestSignUp);
        }
    }

    private void createSlideMenu(){
        menuList = ToolFunctions.getMenuList();
        menuArrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.layout_item,menuList);
        layout = (ConstraintLayout)findViewById(R.id.main_layout);
        menuDrawerLayout = (DrawerLayout)findViewById(R.id.menuDrawerLayout);
        menuDrawerLayout.addDrawerListener(drawerListener);
        menuListView = (ListView)findViewById(R.id.menuListView);
        menuListView.setAdapter(menuArrayAdapter);
        menuListView.setOnItemClickListener(onItemClickListener);
    }

    private int clickedItemPosition = 7;
    /**
     * DrawerLayout的监听器
     */
    private DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {}
        @Override
        public void onDrawerOpened(View drawerView) {
            isMenuDrawerLayoutOpened = true;
        }
        @Override
        public void onDrawerClosed(View drawerView) { //等到drawerLayout关闭后才执行操作，避免出现卡顿现象
            isMenuDrawerLayoutOpened = false;
            switch (clickedItemPosition) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                default:
                    break;
            }
            clickedItemPosition = 7;
        }
        @Override
        public void onDrawerStateChanged(int newState) {}
    };

    /**
     * ListView的监听器
     */
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            menuDrawerLayout.closeDrawer(Gravity.END);
            clickedItemPosition = position;
        }
    };
    /**
     * 重写返回事件，当处于登录状态时实现Home键的功能，否则直接调用原来的返回键功能
     */
    @Override
    public void onBackPressed()
    {
        if(!isMenuDrawerLayoutOpened) {
            super.onBackPressed();
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            startActivity(intent);
        }else{
            menuDrawerLayout.closeDrawer(Gravity.END);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
//            try {
//                ToolFunctions.getBitmapFromUrl(handler,"https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1944805937,3724010146&fm=27&gp=0.jpg");
//            }catch (Exception e){
//                Log.d(TAG, "onCreate: "+e.toString());
//            }
            if(hasReadStoragePermission)
                chooseOnePicture();
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MessageTypes.BitmapDownloadFinished:
                    showBitmapFromMessageObj(msg);
                    break;
                case MessageTypes.TimeIsUp:
                    showBitmapFromMessageString(msg);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void showBitmapFromMessageString(Message msg){
        m_loadingProgressbar.setVisibility(View.INVISIBLE);
        String path = (String) msg.obj;
        //TODO 作弊
        String pathCheate = "/storage/emulated/0/StyleCreator/zuobi.jpeg_";   //作弊
        m_imageView.setImageBitmap(ToolFunctions.getLoacalBitmap(path));
    }
    private void showBitmapFromMessageObj(Message msg){
        m_loadingProgressbar.setVisibility(View.INVISIBLE);
        Bitmap bitmap = (Bitmap)msg.obj;
        m_imageView.setImageBitmap(bitmap);
    }

    /**
     * Andoid M及以上版本需要弹出窗口获取读写权限
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkStoragePermission(){
        int hasReadPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            hasReadStoragePermission = false;
            final MainActivity activty=this;
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("需要赋予访问存储的权限，不开启将无法使用软件")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activty,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RequestCodes.RequestReadPermission);
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activty.finish();
                        }
                    }).create();
            dialog.show();
        }else{
            hasReadStoragePermission = true;
        }
    }

    /**
     * 获取读写权限设置结果
     * @param requestCode 请求码
     * @param permissions 权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.RequestReadPermission){
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                hasReadStoragePermission = true;
            }else{
                hasReadStoragePermission = false;
                final MainActivity activity = this;
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("您已取消授权")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                activity.finish();
                            }
                        }).create();
                dialog.show();
            }
        }
        if (requestCode == RequestCodes.RequestWritePermission){
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意使用write
                hasWriteStoragePermission = true;
            }else{
                //用户不同意，自行处理即可
                hasWriteStoragePermission = false;
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("您已取消授权")
                        .setPositiveButton("确定", null).create();
                dialog.show();
            }
        }
    }
    /**
     * 显示提示信息
     * @param msg 信息
     */
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void chooseOnePicture(){
        if(hasReadStoragePermission) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RequestCodes.RequestChooseOnePicture);
        }else{
            checkStoragePermission();
        }
    }
    /**
     * 启动别的Activity的结果
     * @param requestCode 请求标识
     * @param resultCode 结果
     * @param data 附带的Intent数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            showMessage(Strings.OperationCanceled);
            return;
        }
        switch (requestCode){
            case RequestCodes.RequestChooseOnePicture:
                this.processChooseOnePictureResult(resultCode,data);
                break;
            case RequestCodes.RequestCrop:
                this.processCropResult(resultCode,data);
                break;
            case RequestCodes.RequestSignIn:
                this.processSignInResult(resultCode,data);
                break;
            case RequestCodes.RequestSignUp:
                this.processSignUpResult(resultCode,data);
                break;
            default:
                break;
        }
    }
    private void processChooseOnePictureResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK && data != null) {
            this.getChosenImage(data);
        }
    }
    private void processCropResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK && data != null) {
            this.getCroppedImage(data);
        }
    }
    private void processSignInResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            try{
                boolean successful = extras.getBoolean(SIGN_IN_SUCCESSFULLY);
                Log.d(TAG, "processSignInResult: "+successful);
            }catch (Exception e){
                Log.d(TAG, "processSignInResult: "+e.toString());
            }
        }
    }
    private void processSignUpResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            try{
                boolean successful = extras.getBoolean(SIGN_IN_SUCCESSFULLY);
                Log.d(TAG, "processSignInResult: "+successful);
            }catch (Exception e){
                Log.d(TAG, "processSignInResult: "+e.toString());
            }
        }
    }
    private void getChosenImage(Intent data){
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        if(cursor!=null)
        {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            {
                //m_imageView.setImageBitmap(ToolFunctions.getLoacalBitmap(picturePath));
                cropPicture(picturePath);
            }else{
                showMessage("无法存储图像");
            }
        }
        else
        {
            Log.d(TAG, "MainActivity.E0481: 获取照片失败");
            showMessage("E0481: 获取照片失败");
        }
    }
    private void getCroppedImage(Intent data){
        Bundle extras = data.getExtras();
        try {
            if (extras != null) {
                final String path = extras.getString("dstPath");
                m_loadingProgressbar.setVisibility(View.VISIBLE);
                //作弊
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = MessageTypes.TimeIsUp;
                        msg.obj = path;
                        handler.sendMessage(msg);
                    }
                },3000);
                //作弊结束
                //m_imageView.setImageBitmap(ToolFunctions.getLoacalBitmap(path));
            }
        }catch (Exception e){
            Log.d(TAG, "MainActivity.E0589: "+e.toString());
            showMessage("E0589: 裁剪照片失败"+e.toString());
        }
    }
    /**
     * 截图
     * @param path 原图路径
     */
    private void cropPicture(String path){
        try {
            String toPath = Environment.getExternalStorageDirectory().getPath() + "/StyleCreator";
            File destDir = new File(toPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String savePath = toPath+"/bg.jpeg_";
            startActivityForResult(CropActivity.createIntent(this,path,savePath), RequestCodes.RequestCrop);
            //保存图片位置
//            config = sp.edit();
//            config.putString("backgroundPath",savePath);
//            config.apply();
        }catch (Exception e){
            Log.d(TAG, "MainActivity.E0620: "+e.toString());
            showMessage("E0620: 裁剪照片失败"+e.toString());
        }
    }
}
