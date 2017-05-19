package com.david.common.util.test;

import com.david.common.util.Prints;
import org.junit.Test;

/**
 * Created by jiakang on 2017/5/19.
 */
public class PrintsTest {

    @Test
    public void testPrint() {
        System.out.println("print start");
        Prints.print("hello world");
        Prints.println("I am David");
        System.err.println("err start");
        Prints.printErr("What's wrong?");
        Prints.printlnErr("Oh, I have no idea");
        System.err.println("err end");
        System.out.println("print end");
    }
}
