package com.david.common.util;

import org.apache.logging.log4j.message.ParameterizedMessage;

/**
 * 打印。用于打印相关的封装
 * Created by jiakang on 2017/5/19.
 */
public class Prints {

    public static void print(String str) {
        System.out.print(str);
    }

    public static void print(String format, Object... params) {
        System.out.print(getFormattedMessage(format, params));
    }

    public static void println(String str) {
        System.out.println(str);
    }

    public static void println(String format, Object... params) {
        String resultMsg = getFormattedMessage(format, params);
        System.out.println(resultMsg);
    }

    public static void printErr(String str) {
        System.err.print(str);
    }

    public static void printErr(String format, Object... params) {
        System.err.print(getFormattedMessage(format, params));
    }

    public static void printlnErr(String str) {
        System.err.println(str);
    }

    public static void printlnErr(String format, Object... params) {
        System.err.println(getFormattedMessage(format, params));
    }

    private static String getFormattedMessage(String format, Object[] params) {
        return new ParameterizedMessage(format, params).getFormattedMessage();
    }
}
