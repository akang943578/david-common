package com.david.common.util.test;

import com.david.common.util.Prints;
import org.junit.Test;

/**
 * Created by jiakang on 2017/5/19.
 */
public class PrintsTest {

    @Test
    public void testPrint() {
        Prints.print("hello world");
        Prints.print("hello, {}, I'm {}", "David", "Lily");
        Prints.println("I am David");
        Prints.println("I am {}", "a dog");
        Prints.printErr("What's wrong?");
        Prints.printErr("What's wrong, {}?", "David");
        Prints.printlnErr("Oh, I have no idea");
        Prints.printlnErr("Oh, I want to find {}, he is {}", "Monkey", "superman");
    }
}
