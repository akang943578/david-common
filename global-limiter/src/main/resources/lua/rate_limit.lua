--
-- Created by IntelliJ IDEA.
-- User: jiakang
-- Date: 2017/12/5
-- Time: 19:49
-- To change this template use File | Settings | File Templates.
-- 从redis申请许可
-- 返回申请到的许可数
--

-- 操作的redis key
local rate_limit_key = KEYS[1]
-- 每秒最大的qps许可数
local max_permits = ARGV[1]
-- 此次申请的许可数
local incr_by_count_str = ARGV[2]

-- 当前已用的许可数
local currentStr = redis.call('get', rate_limit_key)
local current
if currentStr then
    current = tonumber(currentStr)
else
    current = 0
end

-- 剩余可分发的许可数
local remain_permits = tonumber(max_permits) - current
local incr_by_count = tonumber(incr_by_count_str)
-- 如果可分发的许可数小于申请的许可数，只能申请到可分发的许可数
if remain_permits < incr_by_count then
    incr_by_count = remain_permits
end

-- 将此次实际申请的许可数加到redis key里面
local result = redis.call('incrby', rate_limit_key, incr_by_count)
-- 初次操作redis key设置1秒的过期
if result == incr_by_count then
    redis.call('expire', rate_limit_key, 1)
end

-- 返回实际申请到的许可数
return incr_by_count
