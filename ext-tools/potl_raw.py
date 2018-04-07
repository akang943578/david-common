#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018/3/1 16:46
# @Author  : jiakang
# @File    : potl_raw.py
# @Software: IntelliJ IDEA

import basetl, alitl_raw, nortl_raw


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
            if ali_ips_str:
                print('connect ips successfully.')
                basetl.print_ips_info(ips)

                ali_user, ali_key = alitl_raw.get_ali_login_info()
                while True:
                    try:
                        command = raw_input('%s@both-multiple-ssh> ' % ali_user)
                        if command == 'exit':
                            exit(0)
                        elif command == 'show ips':
                            basetl.print_ips_info(ips)
                        elif command.strip() != '':
                            alitl_raw.ali_multi_run_cmd(command, ali_ips_str, ali_user, ali_key)

                            if normal_ips_str:
                                self.normal_tool.execute_multi_ips(normal_ips_str, command)
                    except KeyboardInterrupt as e:
                        continue

            elif normal_ips_str:
                self.normal_tool.execute_multi_ips(normal_ips_str)
            else:
                print('connect failed. no ips to connect')
