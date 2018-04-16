package com.david.common.util.test;

import com.david.common.util.CommandExecutor;
import org.junit.Test;

public class CommandExecutorTest {

    @Test
    public void testCommand() {
//        String pwdString = exec("pwd").toString();
//        String netsString = exec("netstat -nat|grep -i \"80\"|wc -l").toString();
        String cmd = "ansible -i invent.txt sde -a free";
        cmd = "ls";
//        cmd = "pwd";
//        cmd = "ansible sde -a free";
//        cmd = "ansible -i /home/fsdevops/invent.txt sde -a free -k";
//        cmd = "ansible -u fsdevops -i /home/fsdevops/invents/invent_b5325312-6d5a-49ce-9b06-fa101695b0021460983896131.txt all -a ls";
//		cmd = "ansible -i invent.txt all -a free";
//        cmd = "df -h";
        cmd = "ffmpeg -i /home/fsdevops/workspace/fs-qixin-extension/fs-qixin-extension-common/audioFile/test.opus -vn -f mp3 -y /home/fsdevops/workspace/fs-qixin-extension/fs-qixin-extension-common/audioFile/test_java_cmd_tomp3.mp3";
        String result = CommandExecutor.runCommand(cmd);

        System.out.println("==========获得值=============");
        System.out.println(result);
    }
}
