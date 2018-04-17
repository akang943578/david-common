package com.david.common.util.test;

import com.david.common.util.Consoles;
import org.junit.Test;


/**
 * Created by jiakang on 2018/4/17
 *
 * @author jiakang
 */
public class ConsolesTest {

    @Test
    public void testInfo() {
        Consoles.info("person info, name:{}, age:{}", "David", 23);
        Consoles.debug("person info, name:{}, age:{}", "David", 23);
    }
}