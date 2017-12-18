package com.fishpool.stylecreator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import static com.fishpool.stylecreator.ConstValues.*;

public class LoginActivity extends AppCompatActivity {
    //LoginActivity
    public final static String IS_SIGN_UP = "isSignUp";
    public final static String SIGN_IN_SUCCESSFULLY = "signInSuccessfully";
    public final static String CONFIG_LOGIN  = "config_login";
    //config tag
    public final static String TagEmail = "tag_email";
    public final static String TagPassword = "tag_password";
    public final static String TagConfirmPassword = "tag_confirm_password";
    public final static String TagAlreadySignIn = "tag_already_sign_in";
    public final static String TagAutoLogin = "tag_auto_login";
    //类变量
    private boolean isSignUp;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private CheckBox cbAutoLogin;
    private Button btLogin;
    SharedPreferences.Editor config;

    private SharedPreferences preferences;

    public static Intent createIntent(AppCompatActivity activity,
                                      boolean isSignUp){
        Intent intent = new Intent(activity,LoginActivity.class);
        intent.putExtra(IS_SIGN_UP,isSignUp);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etPassword = (EditText)findViewById(R.id.etPassword);
        etConfirmPassword = (EditText)findViewById(R.id.etConfirmPassword);
        cbAutoLogin = (CheckBox)findViewById(R.id.cbAutoLogin);
        btLogin = (Button)findViewById(R.id.btLogin);
        btLogin.setOnClickListener(onClickListener);
        init();
        etEmail.addTextChangedListener(new EditTextWatcher(etEmail));
        etPassword.addTextChangedListener(new EditTextWatcher(etPassword));
        cbAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: "+isChecked);
                config = preferences.edit();
                config.putString(TagEmail, etEmail.getText().toString().trim());
                config.putBoolean(TagAutoLogin,isChecked);
                if(isChecked) {
                    config.putString(TagPassword, etPassword.getText().toString().trim());
                }else{
                    config.putString(TagEmail, "");
                }
                config.apply();
            }
        });
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btLogin:
//                    returnToMainActivity(true,RESULT_OK);
                    if(isSignUp){
                        signUp();
                    }else{
                        signIn();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 监听编辑框项
     */
    private class EditTextWatcher implements TextWatcher {

        private int viewId;
        EditTextWatcher(View view){
            this.viewId = view.getId();
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            switch (viewId){
                case R.id.etEmail:
                    config = preferences.edit();
                    config.putString(TagEmail,s.toString().trim());
                    config.apply();
                    break;
                case R.id.etPassword:
                    if(cbAutoLogin.isChecked()) {
                        config = preferences.edit();
                        config.putString(TagPassword, s.toString().trim());
                        config.apply();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MessageTypes.SignIn:
                    processSignInResult(msg);
                    break;
                case MessageTypes.SignUp:
                    processSignUpResult(msg);
                    break;
            }
            return false;
        }
    });

    private void processSignInResult(Message message){
        String result = (String)message.obj;
        showMessage(result);
        config = preferences.edit();
        if(result.equals(Strings.SignInSucessfully)){
            config.putBoolean(TagAlreadySignIn,true);
            returnToMainActivity(true,RESULT_OK);
        }else{
            config.putBoolean(TagAlreadySignIn,false);
            returnToMainActivity(false,RESULT_OK);
        }
        config.apply();
    }
    private void processSignUpResult(Message message){
        String result = (String)message.obj;
        showMessage(result);
        returnToMainActivity(true,RESULT_OK);
        config = preferences.edit();
        if(result.equals(Strings.SignUpSucessfully)){
            config.putBoolean(TagAlreadySignIn,true);
            returnToMainActivity(true,RESULT_OK);
        }else{
            config.putBoolean(TagAlreadySignIn,false);
            returnToMainActivity(false,RESULT_OK);
        }
        config.apply();
    }
    private void init(){
        preferences = getApplicationContext().getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        try {
            Intent intent = this.getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                isSignUp = bundle.getBoolean(IS_SIGN_UP);
                if(!isSignUp){//不是注册，隐藏确认密码框
                    etConfirmPassword.setVisibility(View.INVISIBLE);
                }else{
                    cbAutoLogin.setVisibility(View.INVISIBLE);
                }
            }
        }catch (Exception e){
            Logger.d(TAG, "LoginActivity.E0104: "+e.toString());
            showMessage(e.toString());
            returnToMainActivity(false,RESULT_CANCELED);
        }
        readUserInfo();
    }
    private void returnToMainActivity(boolean isSignIn,int result){
        Intent intent = new Intent();
        intent.putExtra(SIGN_IN_SUCCESSFULLY, isSignIn);
        setResult(result, intent);
        finish();
    }
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }
    private void signIn(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if(email.isEmpty()){
            showMessage(Strings.EmailIsEmpty);
            return;
        }
        if(password.isEmpty()){
            showMessage(Strings.PasswordIsEmpty);
            return;
        }
        ToolFunctions.signIn(email,password,handler);
    }
    private void signUp(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if(email.isEmpty()){
            showMessage(Strings.EmailIsEmpty);
            return;
        }
        if(password.isEmpty()){
            showMessage(Strings.PasswordIsEmpty);
            return;
        }
        if(confirmPassword.isEmpty()){
            showMessage(Strings.ComfirmPasswordIsEmpty);
            return;
        }
        if(!password.equals(confirmPassword)){
           showMessage(Strings.PasswordNotEqualWithConfirmPassword);
        }
        ToolFunctions.signUp(email,password,confirmPassword,handler);
    }
    private void readUserInfo(){
        if(isSignUp){   //如果是注册，停止读取用户数据
            return;
        }
        boolean autoLogin = preferences.getBoolean(TagAutoLogin,false);
        cbAutoLogin.setChecked(autoLogin);
        if(autoLogin){
            etEmail.setText(preferences.getString(TagEmail,""));
            etPassword.setText(preferences.getString(TagPassword,""));
        }
    }
}