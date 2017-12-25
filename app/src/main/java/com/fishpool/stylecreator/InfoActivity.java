package com.fishpool.stylecreator;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private TextView tvName;
    private TextView tvEmail;
    private TextView tvImageCount;
    private Button btSignOut;

    public static Intent createIntent(AppCompatActivity activity){
        return new Intent(activity,InfoActivity.class);
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
        tvImageCount.setText(userInfo.get("image_count"));
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
            finish();
        }else{
            showMessage("注销失败");
        }
    }
}
