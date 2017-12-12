package com.fishpool.stylecreator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import static com.fishpool.stylecreator.ConstValues.*;

public class MainActivity extends AppCompatActivity {

    private boolean hasWriteStoragePermission;
    private boolean hasReadStoragePermission;

    private final static boolean isHeigerThanAnroidM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    private ImageView m_imageView;
    private Button m_btChoosePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_imageView = (ImageView) findViewById(R.id.imageView);
        m_btChoosePicture = (Button)findViewById(R.id.button);
        m_btChoosePicture.setOnClickListener(onClickListener);
        if(isHeigerThanAnroidM)
            checkStoragePermission();
        else{
            hasWriteStoragePermission = true;
            hasReadStoragePermission = true;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                ToolFunctions.getBitmapFromUrl(handler,"https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=1944805937,3724010146&fm=27&gp=0.jpg");
            }catch (Exception e){
                Log.d(TAG, "onCreate: "+e.toString());
            }
        }
    };

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MessageTypes.BitmapDownloadFinished:
                    showBitmap(msg);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void showBitmap(Message msg){
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
                    .setMessage("需要赋予访问存储的权限，不开启将无法更换软件背景")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(activty,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                    RequestCodes.RequestReadPermission);
                        }
                    }).setNegativeButton("取消", null).create();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.RequestReadPermission){
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                hasReadStoragePermission = true;
            }else{
                hasReadStoragePermission = false;
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setMessage("您已取消授权")
                        .setPositiveButton("确定", null).create();
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
    private void showMessage(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
}
