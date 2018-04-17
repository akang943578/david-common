package com.david.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.SimpleMessageFactory;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.Properties;

/**
 * Created by jiakang on 2018/4/17
 * 以log的格式输出到控制台
 *  Consoles.info("person info, name:{}, age:{}", "David", 23);
 * 输出：person info, name:David, age:23
 * @author jiakang
 */
public class Consoles {

    private static final Logger out = new SimpleLogger(Consoles.class.getName(), Level.ALL, true, true, true, true,
            "yyyyMMdd HH:mm:ss.SSS", null, new PropertiesUtil(new Properties()), System.out);

    public static void debug(String msg) {
        out.debug(msg);
    }

    public static void debug(String format, Object... params) {
        out.debug(format, params);
    }

    public static void info(String msg) {
        out.info(msg);
    }

    public static void info(String format, Object... params) {
        out.info(format, params);
    }

    public static void warn(String msg) {
        out.warn(msg);
    }

    public static void warn(String format, Object... params) {
        out.warn(format, params);
    }

    public static void error(String msg) {
        out.error(msg);
    }

    public static void error(String format, Object... params) {
        out.error(format, params);
    }
}