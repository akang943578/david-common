#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018/2/27 13:52
# @Author  : jiakang
# @File    : alitl_raw.py
# @Software: IntelliJ IDEA

import basetl
import os
from multiprocessing import Process


def get_ali_login_info():
    ali_user = ''
    ali_key = ''
    home = os.environ.get('HOME')

    with open('%s/.wtool_login' % home) as f:
        for line in f:
            kv_list = line.split('=')
            if len(kv_list) > 1:
                key = kv_list[0].strip()
                if key[0:1] != '#':
                    value = kv_list[1].split('#')[0].strip()
                    if key == 'ali_user':
                        ali_user = value
                    elif key == 'ali_key':
                        ali_key = value
    if ali_user == '' or ali_key == '':
        print('ali_user or ali_key is not defined.')
        exit(0)
    return ali_user, ali_key


def ip_cmd_process_runner(ip, cmd, ali_user, ali_key):
    cmd = cmd.replace('\\\\', '\\')
    cmd = cmd.replace('\'', '"')
    cmd = cmd.replace('"', '\\"')

    expect_params = '''
            set timeout -1
            log_user 0
            spawn ssh %s@%s -i %s "%s"
            log_user 1

            expect {
                "yes/no" {send -- "yes\n"; exp_continue}
            }
            ''' % (ali_user, ip, ali_key, cmd)

    ip_cmd = 'expect -c \'%s\'' % expect_params

    # ip_cmd = 'ssh %s@%s -i %s "%s"' % (ali_user, ip, ali_key, cmd)
    try:
        ip_result = basetl.run_cmd(ip_cmd)
    except KeyboardInterrupt as e:
        return

    result_lines = ip_result.split('\n')
    for line in result_lines:
        if line == '':
            continue
        print('%s: %s' % (ip, line))


def ali_multi_run_cmd(curr_cmd, ips, ali_user, ali_key):
    process_list = []

    ip_list = ips.split(',')
    for ip in ip_list:
        p = Process(target=ip_cmd_process_runner, args=(ip, curr_cmd, ali_user, ali_key))
        process_list.append(p)
        p.start()

    try:
        for p in process_list:
            p.join()
    except KeyboardInterrupt as e:
        pass


class AliTool(basetl.BaseTool):

    def execute_single_ip(self, ip, command=''):
        ali_user, ali_key = get_ali_login_info()
        os.system('ssh %s@%s -i %s "%s"' % (ali_user, ip, ali_key, command))

    def execute_multi_ips(self, ips, command=''):
        if ips:
            print('connect successfully.')
            basetl.print_ips_info(ips)
            ali_user, ali_key = get_ali_login_info()

            if command:
                try:
                    ali_multi_run_cmd(command, ips, ali_user, ali_key)
                except KeyboardInterrupt as e:
                    return

            else:
                while True:
                    try:
                        command = raw_input('%s@ali-multiple-ssh> ' % ali_user)
                        if command == 'exit':
                            exit(0)
                        elif command == 'show ips':
                            basetl.print_ips_info(ips)
                        elif command.strip() != '':
                            ali_multi_run_cmd(command, ips, ali_user, ali_key)
                    except KeyboardInterrupt as e:
                        continue
        else:
            print('connect failed. ips_count: 0')
