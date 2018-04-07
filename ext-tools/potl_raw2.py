#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018/3/1 16:46
# @Author  : jiakang
# @File    : potl_raw.py
# @Software: IntelliJ IDEA

import basetl, alitl_raw, nortl_raw
import os
from multiprocessing import Process
import time


def deal_with_both_ips(normal_ips, ali_ips, func=None):
    p = Process(target=ali_command_runner, args=(ali_ips, func))
    p.start()

    expect_params = '''
                    set timeout -1
                    spawn wtool login %s
    
                    expect {
                        "multiple-ssh" {
                            expect_user -re "(.*)\n";
                            set command "$expect_out(1,string)";
                            send -- "$command\n"; 
                            set commands_file [open /tmp/commands.log a];
                            puts $commands_file "$command"; close $commands_file;
                            exp_continue;
                        }
                    }
                    
                    interact
                    ''' % normal_ips

    commands = 'expect -c \'%s\'' % expect_params
    os.system(commands)


def ali_command_runner(ali_ips, func):
    expect_params = '''
            set commands_file [open /tmp/commands.log w];
            puts $commands_file " "; close $commands_file;
        '''
    commands = 'expect -c \'%s\'' % expect_params
    os.system(commands)

    def follow(file):
        file.seek(0, 2)
        while True:
            try:
                line = file.readline()
                if not line:
                    time.sleep(0.1)
                    continue
                yield line
            except KeyboardInterrupt as e:
                continue

    watch_file = '/tmp/commands.log'
    logfile = open(watch_file, 'r')
    loglines = follow(logfile)
    for line in loglines:
        if line and ali_ips:
            # print('line: %s, ali_pis: %s' % (line, ali_ips))
            ali_user, ali_key = alitl_raw.get_ali_login_info()
            alitl_raw.ali_multi_run_cmd(line, ali_ips, ali_user, ali_key)


class PoolTool(basetl.BaseTool):

    def __init__(self):
        self.normal_tool = nortl_raw.NormalTool()
        self.ali_tool = alitl_raw.AliTool()

    def execute_single_ip(self, ip, command=''):
        if basetl.is_aliyun(ip):
            self.ali_tool.execute_single_ip(ip, command)
        else:
            self.normal_tool.execute_single_ip(ip, command)

    def execute_multi_ips(self, ips, command=''):
        normal_ips, ali_ips, both_ips = [], [], []
        basetl.separated_ips(ips, normal_ips, ali_ips, both_ips)

        normal_ips_str, ali_ips_str = '', ''
        if normal_ips:
            normal_ips_str = ','.join(normal_ips)
        if ali_ips:
            ali_ips_str = ','.join(ali_ips)

        if command:
            if ali_ips_str:
                self.ali_tool.execute_multi_ips(ali_ips_str, command)
            if normal_ips_str:
                self.normal_tool.execute_multi_ips(normal_ips_str, command)

        else:
            if normal_ips_str:
                if ali_ips_str:
                    deal_with_both_ips(normal_ips_str, ali_ips_str, self.ali_tool.execute_multi_ips)
                else:
                    self.normal_tool.execute_multi_ips(normal_ips_str)

            elif ali_ips_str:
                self.ali_tool.execute_multi_ips(ali_ips_str)

            else:
                print('connect failed. no ips to connect')
