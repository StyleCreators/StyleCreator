package com.fishpool.stylecreator;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.fishpool.stylecreator.ConstValues.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.fishpool.stylecreator.ConstValues.TAG;

public class InfoActivity extends AppCompatActivity {
    public final static String Tag_SignOutResult = "SignOutResult";
    public final static String Tag_ImageCount = "ImageCount";

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvImageCount;
    private Button btSignOut;

    public static Intent createIntent(AppCompatActivity activity,int imageCount){
        Intent intent = new Intent(activity,InfoActivity.class);
        intent.putExtra(Tag_ImageCount,imageCount);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        tvName = (TextView)findViewById(R.id.tvName);
        tvEmail = (TextView)findViewById(R.id.tvEmail);
        tvImageCount = (TextView)findViewById(R.id.tvImageCount);
        btSignOut = (Button)findViewById(R.id.btSignOut);
        btSignOut.setOnClickListener(onClickListener);

        if(ToolFunctions.checkLogin(getApplicationContext())){
            init();
        }else{
            showMessage(Strings.YouAreNotSignIn);
        }
    }
    private void init(){
        HashMap<String,String> userInfo = ToolFunctions.getUserInfo(getApplicationContext());
        tvName.setText(userInfo.get("name"));
        tvEmail.setText(userInfo.get("email"));
        try {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int imageCount = bundle.getInt(Tag_ImageCount);
                String s = "照片数量:"+imageCount+"张";
                tvImageCount.setText(s);
            }
        }catch (Exception e){
            Log.d(TAG, "Info.E0061: "+e.toString());
        }
//        tvImageCount.setText(userInfo.get("image_count"));
    }
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btSignOut:
                    signOut();
                    break;
                default:
                    break;
            }
        }
    };

    private void signOut(){
        if(ToolFunctions.signOut(getApplicationContext())){
            showMessage("注销成功！");
            returnToMainActivity(true,RESULT_OK);
            finish();
        }else{
            showMessage("注销失败");
            returnToMainActivity(false,RESULT_OK);
        }
    }
    /**
     * 返回上一个Activity，并将保存路径放在Intent中返回
     * @param isSignOut 是否注销成功
     * @param result 结果
     */
    private void returnToMainActivity(boolean isSignOut,int result){
        Intent intent = new Intent();
        intent.putExtra(Tag_SignOutResult, isSignOut);
        setResult(result, intent);
        finish();
    }
}
