package com.david.common.util;

import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Java调用命令行，并获取返回结果
 * Created by jiakang on 2018/4/15.
 */
public class CommandExecutor {

    public static String runCommand(String cmd) {
        // /bin/sh -c: 如果-c 选项存在，命令就从字符串中读取。如果字符串后有参数，他们将会被分配到参数的位置上，从$0开始。
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmdA);// 启动另一个进程来执行命令
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        final StringBuilder inputBuilder = new StringBuilder();
        final StringBuilder errorBuilder = new StringBuilder();

        try (InputStream inputStream = p.getInputStream();
             InputStream errorStream = p.getErrorStream()) {

            Thread inputThread = new InputStreamThread(inputStream, inputBuilder);
            Thread errorThread = new InputStreamThread(errorStream, errorBuilder);

            inputThread.start();
//			inputThread.join();
            errorThread.start();
//			errorThread.join();

            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String tempResult = inputBuilder.toString();
        return tempResult.length() == 0 ? errorBuilder.toString() : tempResult;
    }

    @AllArgsConstructor
    private static class InputStreamThread extends Thread {

        private InputStream is;
        private StringBuilder sb;

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
