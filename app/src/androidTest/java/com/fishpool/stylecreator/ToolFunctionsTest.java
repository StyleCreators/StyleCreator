package com.fishpool.stylecreator;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)

public class ToolFunctionsTest {
    @Test
    public void testGetLocalBitmap() throws Exception {
        //test1
        String path = "";
        Bitmap bm = ToolFunctions.getLocalBitmap(path);
        assertEquals(bm==null,true);
        //test2
        path = null;
        bm = ToolFunctions.getLocalBitmap(path);
        assertEquals(bm==null,true);
        //test3
        path = ToolFunctions.getOriginPathPrefix();
        bm = ToolFunctions.getLocalBitmap(path);
        assertEquals(bm==null,true);
        //test4
        path = ToolFunctions.getOriginPathPrefix()+"466576866";
        bm = ToolFunctions.getLocalBitmap(path);
        assertEquals(bm==null,false);
    }

    @Test
    public void testGetLocalSmallBitmap() throws Exception {
        //test1
        String path = "";
        Bitmap bm = ToolFunctions.getLocalSmallBitmap(path);
        assertEquals(bm==null,true);
        //test2
        path = null;
        bm = ToolFunctions.getLocalSmallBitmap(path);
        assertEquals(bm==null,true);
        //test3
        path = ToolFunctions.getOriginPathPrefix();
        bm = ToolFunctions.getLocalSmallBitmap(path);
        assertEquals(bm==null,true);
        //test4
        path = ToolFunctions.getOriginPathPrefix()+"466576866";
        bm = ToolFunctions.getLocalSmallBitmap(path);
        assertEquals(bm==null,false);
    }

    @Test
    public void testGetHttpBitmap() throws Exception{
        String url1=null;
        String url2="";
        String url3="https://ss0.bdstatic.com";
        String url4="https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1521193057,2367631835&fm=27&gp=0.jpg";

        //test1
        Bitmap bm = ToolFunctions.getHttpBitmap(url1);
        assertEquals(bm==null,true);
        //test2
        bm = ToolFunctions.getHttpBitmap(url2);
        assertEquals(bm==null,true);
        //test3
        bm = ToolFunctions.getHttpBitmap(url3);
        assertEquals(bm==null,true);
        //test4
        bm = ToolFunctions.getHttpBitmap(url4);
        assertEquals(bm==null,false);
    }

    @Test
    public void testCheckLogin() throws Exception{
        //test1
        Context c1 = null;
        boolean isSignIn = ToolFunctions.checkLogin(c1);
        assertEquals(false, isSignIn);
        //test2
        Context c2 = InstrumentationRegistry.getTargetContext();
        isSignIn = ToolFunctions.checkLogin(c2);
        assertEquals(true, isSignIn);
    }

    @Test
    public void testGetCRC32() throws Exception{

    }
}