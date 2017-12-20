package com.fishpool.stylecreator;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class) //注解
public class MainActivityTest {
    @Rule //添加ActivityTestRule，注意泛型，测试MainActivity
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void choosePicture() throws Exception {
    }
}