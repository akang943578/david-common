#!/usr/bin/env python
# -*- coding: utf-8 -*-
# @Time    : 2018/2/27 13:14
# @Author  : jiakang
# @File    : basetl.py
# @Software: IntelliJ IDEA

import subprocess
import json
# this line 'import readline' can not be remove!!!
# import this to read input line of up, down, left, right
import readline, codecs
import sys

reload(sys)
sys.setdefaultencoding('utf-8')


def run_cmd(pool_cmd):
    p = subprocess.Popen(pool_cmd, shell=True, stdout=subprocess.PIPE).stdout
    result = p.read()
    return result


def print_ips_info(ips):
    print('ips_count: %s, ips: %s' % (len(ips.split(',')), ips))


def separated_ips(ips, normal_ips, ali_ips, both_ips):
    for ip in ips.split(','):
        if is_normal(ip):
            normal_ips.append(ip)
        else:
            ali_ips.append(ip)
        both_ips.append(ip)


def is_normal(ip):
    if len(ip) > 5:
        if ip[:5] == '10.85':
            return False
        return True


def is_aliyun(ip):
    return not is_normal(ip)


def parse_pool(pool_name, machine_type):
    normal_ips = []
    ali_ips = []
    both_ips = []

    pool_cmd = 'wtool jpool -p %s' % pool_name
    result = run_cmd(pool_cmd)
    all_ips_dict = json.loads(result)

    if not all_ips_dict:
        print('pool not exist: %s' % pool_name)
        exit(0)

    for pool, ips in all_ips_dict.items():
        separated_ips(ips, normal_ips, ali_ips, both_ips)

    if machine_type == 'aliyun':
        return ali_ips
    elif machine_type == 'normal':
        return normal_ips
    elif machine_type == 'both':
        return both_ips
    return []


def parse_ips(raw_ips, machine_type):
    normal_ips = []
    ali_ips = []
    both_ips = []

    separated_ips(raw_ips, normal_ips, ali_ips, both_ips)

    if machine_type == 'aliyun':
        return ali_ips
    elif machine_type == 'normal':
        return normal_ips
    elif machine_type == 'both':
        return both_ips
    return []


class BaseTool(object):

    def execute_single_ip(self, ip, command=''):
        pass

    def execute_multi_ips(self, ips, command=''):
        pass

    def execute(self, pool_name, ips, command, machine_type):

        ip_list = []
        if pool_name:
            ip_list = parse_pool(pool_name, machine_type)
        elif ips:
            ip_list = parse_ips(ips, machine_type)

        if len(ip_list) == 1:
            self.execute_single_ip(ip_list[0], command)
        else:
            ips = ','.join(ip_list)
            self.execute_multi_ips(ips, command)
