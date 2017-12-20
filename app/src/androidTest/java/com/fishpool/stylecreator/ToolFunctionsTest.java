package com.fishpool.stylecreator;

import android.graphics.Bitmap;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ToolFunctions测试
 */
public class ToolFunctionsTest {
    @Test
    public void getLocalBitmap() throws Exception {
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
        path = ToolFunctions.getOriginPathPrefix()+"-785983706";
        bm = ToolFunctions.getLocalBitmap(path);
        assertEquals(bm==null,false);
    }

    @Test
    public void getLocalSmallBitmap() throws Exception {
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
        path = ToolFunctions.getOriginPathPrefix()+"-785983706";
        bm = ToolFunctions.getLocalSmallBitmap(path);
        assertEquals(bm==null,false);
    }



}