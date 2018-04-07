## wtool的扩展脚本

* 使用方式

```
添加或更新模块
$ wtool addmodule jiakang/ext-tools
如果已经添加过，输入r更新模块
======= jiakang_ext-tools =======
matl           : materials config manage tool
potl           : pool based multi login tool for both normal and aliyun machines
```

* potl: 基于池子的内网和阿里云同时批量登录脚本

```
前提条件：
    需要首先在~/.wtool_login文件中加入${ali_user},${ali_key}两个配置项
        ali_user=jiakang #阿里云登录用户
        ali_key=/Users/jiakang/aliyun.key #阿里云登录key
    检查${ali_key}文件的权限是否600(-rw-------)，如果不是，请首先执行
        chmod 600 ${ali_key}

Usage:     
   执行如下命令进入机器
        交互式：
            1. wtool potl message_flow_proc #以池子为单位批量进入此池子下的机器
            2. wtool potl -h 10.85.18.188 #指定进入单台机器
            3. wtool potl -h 10.77.6.220,10.85.18.189,10.85.18.190 #指定进入某几台机器
            
            进入到交互输入框(出现multiple-ssh> )时，可执行如下命令：
                show ips #查看当前连接的ips
                exit #退出脚本
                any other commands #在登录的机器上批量执行命令
                
        非交互式：
            1. wtool potl message_flow_proc -c "pwd" #以池子为单位批量进入此池子下的机器执行指定命令
            2. wtool potl -h 10.77.6.220 -c "pwd" #指定进入单台机器执行指定命令
            3. wtool potl -h 10.85.18.188,10.85.18.189,10.77.6.220 -c "pwd" #指定进入某几台机器执行指定命令
    
    可选参数
        -a #只操作阿里云机器
        -n #只操作内网机器
        两者最多存在其一。两者都没有时，操作全部机器
        
使用姿势：
    1. 使用 wtool potl message_flow_proc 或 wtool potl -h ips 进入交互环境后，同时操作内网和阿里云
        [缺点] 不稳定，禁用Ctrl+C，不支持方向键
    2. 如果操作的机器里面含有内网，则将其分开。同时打开两个终端，一个执行 wtool potl message_flow_proc -n 或 wtool potl -h ips -n，
       另一个执行 wtool potl message_flow_proc -a 或 wtool potl -h ips -a，分别执行相同命令
        [缺点] 同样的命令需要在两个终端分别执行一次
    由于无法解决python调用expect读取用户输入时的功能按键问题，脚本只能做到这样了，请酌情使用。
```

* matl: 物料配置系统配置查看和同步脚本

```
所操作的池子是message_flow，包括队列机和前端机

Usage:
    1. wtool matl show -t #查看所有机器物料配置最后更新的时间戳
    2. wtool matl show -d #查看所有机器物料配置最后更新的详情
    3. wtool matl refresh -t #更新所有机器的物料配置，并显示此次更新的配置时间戳
    4. wtool matl refresh -d #更新所有机器的物料配置，并显示此次更新的配置详情
    5. wtool matl show -t 10.77.6.220:8090 #查看单台机器物料配置最后更新的时间戳
    6. wtool matl refresh -d 10.75.2.158:8080 #查看单台机器物料配置最后更新的详情
```
