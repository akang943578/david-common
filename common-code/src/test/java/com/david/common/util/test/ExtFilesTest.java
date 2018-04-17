package com.david.common.util.test;

import com.david.common.util.Consoles;
import com.david.common.util.ExtFiles;
import org.junit.Test;

/**
 * Created by jiakang on 2018/4/17
 *
 * @author jiakang
 */
public class ExtFilesTest {

    @Test
    public void testAppend() {
        ExtFiles.append("/Users/jiakang/hello.txt", "wocao nishishei");
    }

    @Test
    public void testAppendLn() {
        ExtFiles.appendLn("/Users/jiakang/hello.txt", "wocao nishishei");
    }

    @Test
    public void testWrite() {
        ExtFiles.write("/Users/jiakang/hello.txt", "nishaya?");
    }

    @Test
    public void readContent() {
        String content = ExtFiles.readContent("/Users/jiakang/hello.txt");
        Consoles.info("content:{}", content);
    }
}