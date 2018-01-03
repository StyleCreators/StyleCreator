package com.fishpool.stylecreator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.fishpool.stylecreator.LoginActivity.CONFIG_LOGIN;
import static com.fishpool.stylecreator.LoginActivity.TagEmail;
import static com.fishpool.stylecreator.LoginActivity.TagPassword;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.*;
import static com.fishpool.stylecreator.ConstValues.TAG;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(
            LoginActivity.class,false,false);

    @BeforeClass
    public static void setUp(){
        Log.d(TAG, "setUp: ");
        //
        Context context = InstrumentationRegistry.getTargetContext();
        SharedPreferences sp = context.getSharedPreferences(CONFIG_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(TagEmail,"123@qq.com");
        editor.putString(TagPassword,"123");
        editor.apply();
    }

    //启动界面
    @Before
    public void loadActivity(){
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context,LoginActivity.class);
        intent.putExtra(LoginActivity.IS_SIGN_UP,false);//不是注册，是登录
        mActivityTestRule.launchActivity(intent);
    }

    //错误的用户名和错误的密码
    @Test
    public void loginActivityTest0() {
        //操作UI
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.etEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("12@qq.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.etPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("12"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btLogin), withText("登录"), isDisplayed()));
        appCompatButton.perform(click());

        //测试结果
        Context context = InstrumentationRegistry.getTargetContext();
        boolean isSignIn = ToolFunctions.checkLogin(context);
        assertEquals(false,isSignIn);
    }

    //正确的用户名和错误的密码
    @Test
    public void loginActivityTest1() {
        //操作UI
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.etEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("123@qq.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.etPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("12"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btLogin), withText("登录"), isDisplayed()));
        appCompatButton.perform(click());

        //测试结果
        Context context = InstrumentationRegistry.getTargetContext();
        boolean isSignIn = ToolFunctions.checkLogin(context);
        assertEquals(false,isSignIn);
    }

    //正确的用户名和密码
    @Test
    public void loginActivityTest2() {
        //操作UI
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.etEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("123@qq.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.etPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("123"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btLogin), withText("登录"), isDisplayed()));
        appCompatButton.perform(click());

        //测试结果
        Context context = InstrumentationRegistry.getTargetContext();
        boolean isSignIn = ToolFunctions.checkLogin(context);
        assertEquals(true,isSignIn);
    }
}
