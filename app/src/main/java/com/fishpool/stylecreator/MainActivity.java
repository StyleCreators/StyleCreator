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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static com.fishpool.stylecreator.ConstValues.*;
import static com.fishpool.stylecreator.LoginActivity.SIGN_IN_SUCCESSFULLY;

public class MainActivity extends AppCompatActivity {

    private boolean hasWriteStoragePermission;
    private boolean hasReadStoragePermission;

    private final static boolean isHeigerThanAnroidM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private ImageView m_imageView;
    private ProgressBar m_loadingProgressbar;
    //滑动相册相关
    private HorizontalScrollView m_horizontalScrollView;
    private LinearLayout mGallery;
    private int[] mImgIds;
    private LayoutInflater mInflater;
    private List<String> imagePaths;

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
        m_loadingProgressbar = (ProgressBar)findViewById(R.id.loadingProgressBar);
        m_loadingProgressbar.setVisibility(View.INVISIBLE);
        //创建滑动菜单
        createSlideMenu();
        //检查权限
        if(isHeigerThanAnroidM) {
            checkStoragePermission();
        }
        else{
            hasWriteStoragePermission = true;
            hasReadStoragePermission = true;
        }
        //检查是否登录，未登录，则启动登录界面
        if(!ToolFunctions.checkLogin(getApplicationContext())){
            startActivityForResult(LoginActivity.createIntent(this,false),RequestCodes.RequestSignIn);
        }
        if(hasReadStoragePermission){
            if(ToolFunctions.checkLogin(getApplicationContext())){
                initGallery();
            }
        }
    }

    private void initGallery() {
//        mImgIds = ToolFunctions.getPictureIds();
        mImgIds = new int[]{R.drawable.add};
        mGallery = (LinearLayout) findViewById(R.id.id_gallery);
        mGallery.setVerticalGravity(Gravity.CENTER_VERTICAL);
        //添加+号的那张图
        {
            ImageView imageView = new ImageView(this);
            imageView.setId(mImgIds[0]);
            imageView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    boolean visible = m_loadingProgressbar.isShown();
                    if (hasReadStoragePermission)
                        if(!visible)
                            chooseOnePicture();
                    else
                        showMessage("Main.E:有图片正在进行风格转换，清稍后再试");
                }
            });
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(mImgIds[0]);
            mGallery.addView(imageView);
        }
        for (String path:ToolFunctions.getOriginImagePaths()) {
            addImage(path);
        }
    }
    private void addImage(String path){
        if(path==null){
            showMessage("Main.E0015:图片路径为空");
            return;
        }
        ImageView imageView = new ImageView(this);
        //imageView.setLayoutParams(layout);
        String[] list = path.split("/");
        long id = Long.parseLong(list[list.length-1]);
        imageView.setId((int)id);
        Bitmap bitMap = ToolFunctions.getLocalSmallBitmap(path);
        bitMap = ToolFunctions.addWhiteBarForBitmap(bitMap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //TODO 这里怎么搞？暂时从本地读取风格化好的照片
                loadStyledImage(v.getId());
            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                imageViewLongClickOption(v);
                return false;
            }
        });
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType. CENTER_CROP);
        imageView.setImageBitmap(bitMap);
        mGallery.addView(imageView);
    }
    private void imageViewLongClickOption(final View v){
        final String[] items = { "更改风格","删除图片"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("图片操作");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        String origin = ToolFunctions.getOriginPathPrefix()+v.getId();
                        showStyleOptionAndUploadImage(origin);
                        break;
                    case 1:
                        removeImage(v);
                }
            }
        });
        listDialog.show();
    }
    private void removeImage(final View v){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(Strings.RemoveImage)
                .setPositiveButton(Strings.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToolFunctions.removePicFromStorage(v.getId());
                        mGallery.removeView(v);
                    }
                }).setNegativeButton(Strings.No, null).create();
        dialog.show();
    }
    /**
     * 根据id来载入风格化好的图片
     * @param id
     */
    private void loadStyledImage(final int id){
        //根据id生成图片路径
        //TODO 完成生成风格图片的代码之后，这里需要修改成getStyledPathPrefix
        String path = ToolFunctions.getStyledPathPrefix()+id;
        //String path = ToolFunctions.getOriginPathPrefix()+id;
        Bitmap bm = ToolFunctions.getLocalBitmap(path);
        if(bm != null) {
            m_imageView.setImageBitmap(bm);
        }else{
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("该图片尚未修改风格，是否修改？")
                    .setPositiveButton(Strings.Yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String origin = ToolFunctions.getOriginPathPrefix()+id;
                            showStyleOptionAndUploadImage(origin);
                        }
                    }).setNegativeButton(Strings.No, null).create();
            dialog.show();
            Log.d(TAG, "Main.E0172: 图片不存在");
        }
    }


    private void createSlideMenu(){
        menuList = ToolFunctions.getMenuList();
        menuArrayAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.item_menu,menuList);
        layout = (ConstraintLayout)findViewById(R.id.main_layout);
        menuDrawerLayout = (DrawerLayout)findViewById(R.id.menuDrawerLayout);
        menuDrawerLayout.addDrawerListener(drawerListener);
        menuListView = (ListView)findViewById(R.id.menuListView);
        menuListView.setAdapter(menuArrayAdapter);
        menuListView.setOnItemClickListener(onItemClickListener);
    }
    /**
     * DrawerLayout的监听器
     */
    private int clickedItemPosition = 7;
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
                    startActivityForResult(LoginActivity.createIntent(MainActivity.this,false),
                            RequestCodes.RequestSignIn);
                    break;
                case 1://注册被点击
                    startActivityForResult(LoginActivity.createIntent(MainActivity.this, true),
                            RequestCodes.RequestSignUp);
                    break;
                case 2:
                    startActivityForResult(InfoActivity.createIntent(MainActivity.this,
                            mGallery.getChildCount()-1), RequestCodes.RequestShowInfo);
                    break;
                case 3:
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
    public void onBackPressed() {
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

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MsgTypes.BitmapDownloadFinished:
                    showAndSaveBitmapFromMessageObj(msg);
                    break;
                case MsgTypes.TimeIsUp:
                    showBitmapFromMessageString(msg);
                    break;
                case MsgTypes.ServerProcessFinished:
                    processServerProcessResult(msg);
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
        //TODO
        String pathCheate = "/storage/emulated/0/StyleCreator/zuobi.jpeg_";   //作弊
        m_imageView.setImageBitmap(ToolFunctions.getLocalBitmap(path));
    }
    private void showAndSaveBitmapFromMessageObj(Message msg){
        m_loadingProgressbar.setVisibility(View.INVISIBLE);
        Bitmap bitmap = (Bitmap)msg.obj;
        m_imageView.setImageBitmap(bitmap);
        //保存风格化后的图片
        String path = ToolFunctions.getStylePathById(msg.arg1);
        if(path==null){
            Log.d(TAG, "Main.E0292: 存储路径获取失败");
            return;
        }
        ToolFunctions.saveBitmapToPath(bitmap,path);
    }
    private void processServerProcessResult(Message msg){
        String result = (String) msg.obj;
        if(result.contains("Error")){
            showMessage("Main.E0300:"+result);
        }else{
            //处理成功,从服务器下载图片
//            addImage(ToolFunctions.getOriginPathByUrl(result));
            ToolFunctions.getBitmapFromUrl(handler,result);
        }
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
                if(ToolFunctions.checkLogin(getApplicationContext())){
                    initGallery();
                }
            }else{
                hasReadStoragePermission = false;
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("您已取消授权")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
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
                if(ToolFunctions.checkLogin(getApplicationContext())){
                    initGallery();
                }
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
//        if(resultCode == RESULT_CANCELED){
//            showMessage(Strings.OperationCanceled);
//            return;
//        }
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
            case RequestCodes.RequestShowInfo:
                this.processShowInfoResult(resultCode,data);
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
                if(successful) {
                    Log.d(TAG, "processSignInResult: " + successful);
                    initGallery();
                }
//                else
//                    finish();
            }catch (Exception e){
                Log.d(TAG, "Main.E0456: "+e.toString());
            }
        }
    }
    private void processSignUpResult(int resultCode, Intent data){
        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            try{
                boolean successful = extras.getBoolean(SIGN_IN_SUCCESSFULLY);
                Log.d(TAG, "processSignUpResult: "+successful);
                if(successful) {
                    Log.d(TAG, "processSignInResult: " + successful);
                    initGallery();
                }
            }catch (Exception e){
                Log.d(TAG, "Main.E0467: "+e.toString());
            }
        }
    }
    private void processShowInfoResult(int resultCode,Intent data){
        if (resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            try{
                boolean signOut = extras.getBoolean(InfoActivity.Tag_SignOutResult);
                if(signOut){
                    mGallery.removeAllViews();
                }
                Log.d(TAG, "processSignOutResult: "+signOut);
            }catch (Exception e){
                Log.d(TAG, "Main.E0481: "+e.toString());
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
                //m_imageView.setImageBitmap(ToolFunctions.getLocalBitmap(picturePath));
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
    /**
     * 截图
     * @param path 原图路径
     */
    private void cropPicture(String path){
        try {
            String toPath = ToolFunctions.getSavePathPrefix();
            File destDir = new File(toPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String savePath = toPath+"bg.jpeg_";
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
    private void getCroppedImage(Intent data){
        Bundle extras = data.getExtras();
        try {
            if (extras != null) {
                final String path = extras.getString("dstPath");
                //Bitmap bitMap = ToolFunctions.getLocalBitmap(path);
                addImage(path);
                showStyleOptionAndUploadImage(path);
            }
        }catch (Exception e){
            Log.d(TAG, "MainActivity.E0589: "+e.toString());
            showMessage("MainActivity.E0589: 裁剪照片失败"+e.toString());
        }
    }
    private void showStyleOptionAndUploadImage(final String path){
        m_loadingProgressbar.setVisibility(View.VISIBLE);
        final int[] styleId = new int[]{1};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("清选择风格");
        //TODO 后期添加更多风格，并且细化风格名称
        final String[] styles = {"风格1", "风格2", "风格3","风格4"};
        //设置一个单项选择下拉框
        builder.setSingleChoiceItems(styles, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                styleId[0] = which;
            }
        });
        builder.setPositiveButton(Strings.Yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String email = LoginActivity.getUserEmail(MainActivity.this);
                if(!ToolFunctions.uploadOriginImage(handler,email,path,styleId[0])) {
                    showMessage("Main.E0470:上传图片失败");
                }
            }
        });
        builder.setNegativeButton(Strings.No, null);
        builder.show();
        m_loadingProgressbar.setVisibility(View.VISIBLE);
    }
}
