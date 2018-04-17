package com.david.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Created by jiakang on 2018/4/17
 * 扩展的File类。支持一次读取文件或写入文件
 * @author jiakang
 */
@Slf4j
public class ExtFiles {
    private static final String LINE_SEP = System.lineSeparator();
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static String readContent(String path) {
        return readContent(path, DEFAULT_CHARSET);
    }

    public static String readContent(String path, Charset charset) {
        return readContent(new File(path), charset);
    }

    public static String readContent(File file) {
        return readContent(file, DEFAULT_CHARSET);
    }

    public static String readContent(File file, Charset charset) {
        String content = null;
        try {
            List<String> lines = Files.readAllLines(file.toPath(), charset);
            content = String.join(LINE_SEP, lines);
        } catch (IOException e) {
            log.warn("readContent error, file:{}, charset:{}", file, charset, e);
        }
        return content;
    }

    public static void write(String path, String content) {
        write(path, content, DEFAULT_CHARSET);
    }

    public static void write(String path, String content, Charset charset) {
        write(new File(path), content, charset);
    }

    public static void write(File file, String content) {
        Charset charset = DEFAULT_CHARSET;
        try {
            Files.write(file.toPath(), content.getBytes(charset), StandardOpenOption.WRITE);
        } catch (IOException e) {
            log.warn("write error, file:{}, content:{}, charset:{}", file, content, charset, e);
        }
    }

    public static void write(File file, String content, Charset charset) {
        optContent(file, content, charset, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void append(String path, String content) {
        append(path, content, DEFAULT_CHARSET);
    }

    public static void append(String path, String content, Charset charset) {
        append(new File(path), content, charset);
    }

    public static void append(File file, String content) {
        append(file, content, DEFAULT_CHARSET);
    }

    public static void append(File file, String content, Charset charset) {
        optContent(file, content, charset, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static void appendLn(String path, String content) {
        appendLn(path, content, DEFAULT_CHARSET);
    }

    public static void appendLn(String path, String content, Charset charset) {
        appendLn(new File(path), content, charset);
    }

    public static void appendLn(File file, String content) {
        appendLn(file, content, DEFAULT_CHARSET);
    }

    public static void appendLn(File file, String content, Charset charset) {
        append(file, content + LINE_SEP, charset);
    }

    private static void optContent(File file, String content, Charset charset, OpenOption... options) {
        try {
            Files.write(file.toPath(), content.getBytes(charset), options);
        } catch (IOException e) {
            log.warn("write error, file:{}, content:{}, charset:{}, options:{}", file, content, charset, options, e);
        }
    }
}