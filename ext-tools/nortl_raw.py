#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018/2/27 12:38
# @Author  : jiakang
# @File    : nortl_raw.py
# @Software: IntelliJ IDEA

import basetl
import os


class NormalTool(basetl.BaseTool):

    def execute_single_ip(self, ip, command=''):
        self.execute_multi_ips(ip, command)

    def execute_multi_ips(self, ips, command=''):
        if ips:
            if command:
                command = command.replace('\\\\', '\\\\\\\\')
                command = command.replace('\'', '"')
                command = command.replace('"', '\\"')

                expect_params = '''
                    set timeout -1
                    log_user 0
                    spawn wtool login
                    log_user 1
    
                    log_user 0
                    expect {
                        "Last login" {log_user 1; exp_continue}
                        "Any questions, pls visit:" {log_user 0; exp_continue}
                        "Enter:" {send -- "2\n"; exp_continue}
                        "plz input multiple ip addresses" {send -- "%s\n"; exp_continue}
                        "Select output mode" {send -- "c\n"; exp_continue}
                        "multiple-ssh" {
                            send -- "\n%s\n"
                            send -- "exit\n"
                            send -- "q\n"
                        }
                    }
    
                    interact
                    ''' % (ips, command)

                commands = 'expect -c \'%s\'' % expect_params
                os.system(commands)

            else:
                os.system('wtool login %s' % ips)

        else:
            print('connect failed. ips_count: 0')
