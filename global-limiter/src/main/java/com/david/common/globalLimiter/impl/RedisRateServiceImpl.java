package com.david.common.globalLimiter.impl;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.david.common.globalLimiter.RedisRateService;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by jiakang on 2017/12/5.
 * 基于Redis的分布式限速服务
 *
 * 示例：
 *  spring中添加：
 *      <bean id="redisRateService" class="com.david.common.globalLimiter.impl.RedisRateServiceImpl">
 *          <!-- 限速基于redis，需要配置一个redis端口 -->
 *          <constructor-arg name="jedisAddress" value="${jedisAddress}"/>
 *          <!-- 配置限速使用的redis key，注意要在redis端口中保证唯一，否则相互影响 -->
 *          <constructor-arg name="rateLimitKey" value="${rateLimitKey}"/>
 *          <!-- 服务部署的节点数量，用于在redis资源有问题时使用 -->
 *          <constructor-arg name="defaultMcSize" value="${defaultMcSize}"/>
 *          <!-- qps限速值，每秒许可的最大值，最好设置为 (1000/${monitorPeriodInMillis}) 的倍数，避免出现精度问题导致限速不准确 -->
 *          <constructor-arg name="maxPermits" value="${maxPermits}"/>
 *          <!-- 后台线程从redis端口申请许可间隔，默认为50，一般无需更改此值 -->
 *          <!--<constructor-arg name="monitorPeriodInMillis" value="${monitorPeriodInMillis}"/>-->
 *      </bean>
 *  代码中注入bean：
 *      @Resource
 *      private RedisRateService redisRateService;
 *  在需要限速的地方执行：
 *      redisRateService.acquire(); 或
 *      redisRateService.acquire(int permits);
 *
 * 注意：
 *  如果需要更改要限速的值，则只需修改redis端口中${rateLimitKey}_m的值即可，立即生效。无需重新上线。
 *  ${rateLimitKey}_m的值具有永久性，下次上线时
 *      如果使用 public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize) {}
 *          构造器，maxPermits会被设置为redis端口中${rateLimitKey}_m的值；
 *      如果使用 public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize, int maxPermits) {}
 *          构造器，maxPermits会使用注入进来的值，并重设redis端口中${rateLimitKey}_m的值。
 *  所以：
 *      如果希望redis端口中的值永久生效，请使用
 *          public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize) {}
 *      如果希望每次上线重设redis端口中的值，请使用
 *          public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize, int maxPermits) {}
 *
 * @author jiakang
 */
@Slf4j
public class RedisRateServiceImpl implements RedisRateService {

    /** 最大qps的key后缀 */
    private static final String MAX_PERMITS_SUFFIX = "_m";
    /** 当前已用许可数的key后缀 */
    private static final String USED_PERMITS_SUFFIX = "_u";

    public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize) {
        this.jedisAddress = jedisAddress;
        this.rateLimitKey = rateLimitKey;
        this.defaultMcSize = defaultMcSize;
    }

    public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize, int maxPermits) {
        this(jedisAddress, rateLimitKey, defaultMcSize);
        this.maxPermits = maxPermits;
    }

    public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize, int maxPermits, int monitorPeriodInMillis) {
        this(jedisAddress, rateLimitKey, defaultMcSize, maxPermits);
        this.monitorPeriodInMillis = monitorPeriodInMillis;
    }

    /**
     * jedis端口地址
     */
    private String jedisAddress;
    /**
     * 限速使用的key
     * 此key一定要保证在一个${jedisAddress}端口中唯一
     */
    private String rateLimitKey;
    /**
     * 限制的最大qps
     */
    private volatile int maxPermits;
    /**
     * 默认服务机器数（用于lua脚本出错时设置默认的单机qps）
     */
    private int defaultMcSize;
    /**
     * 从redis获取许可的执行间隔
     */
    private int monitorPeriodInMillis = 50;


    /** jedis客户端 */
    private Jedis jedis;

    /** 每秒钟从redis获取许可执行次数 */
    private volatile int applyCountsPerSecond;

    /** 每次从redis申请的许可数 */
    private volatile int applyPermitsPerCount;

    /** 默认每次从redis申请来的许可数，以maxPermits和defaultMcSize计算 */
    private volatile int defaultApplyPermitsPerCount;

    /** redis最大qps的key */
    private String maxPermitsKey;

    /** redis当前已用许可数的key */
    private String usedPermitsKey;

    /** lua脚本加载后的shaKey */
    private String shaKey;

    /** 本地当前存储的许可数 */
    private final AtomicInteger storedPermits = new AtomicInteger();

    @PostConstruct
    public void init() throws Exception {
        checkSotArgs();
        initLocalArgs();
        loadLuaScript();
        startMonitorTokenTask();
    }

    /**
     * 外部参数校验
     */
    private void checkSotArgs() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jedisAddress), String.format("jedisAddress(%s) can not be empty", jedisAddress));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(rateLimitKey), String.format("rateLimitKey(%s) can not be empty", rateLimitKey));
        Preconditions.checkArgument(maxPermits >= 0, String.format("maxPermits(%s) should not be negative", maxPermits));
        Preconditions.checkArgument(defaultMcSize > 0, String.format("defaultMcSize(%s) should be positive", defaultMcSize));
        Preconditions.checkArgument(monitorPeriodInMillis > 0, String.format("applyPermitsPerCount(%s) should be positive", monitorPeriodInMillis));
    }

    /**
     * 初始化本地参数
     */
    private void initLocalArgs() throws Exception {
        String[] split = jedisAddress.split(":");
        String host = split[0];
        int length = split.length;
        if (length == 1) {
            jedis = new Jedis(host);
        } else if (length == 2) {
            int port = Integer.parseInt(split[1]);
            jedis = new Jedis(host, port);
        }
        Preconditions.checkArgument(jedis != null, String.format("jedis(%s) can not be null", jedis));

        maxPermitsKey = rateLimitKey + MAX_PERMITS_SUFFIX;
        usedPermitsKey = rateLimitKey + USED_PERMITS_SUFFIX;

        if (maxPermits > 0) {
            int oldMaxPermits = maxPermits;
            resetMaxPermits(maxPermits);
            String setResult = setMaxPermitsToRedis(maxPermits);
            log.info("resetMaxPermits and reset maxPermitsKey, oldMaxPermits:{}, maxPermitsKey:{}, setResult:{}", oldMaxPermits, maxPermitsKey, setResult);
        } else {
            maxPermits = getMaxPermitsFromRedis();
            Preconditions.checkArgument(maxPermits > 0, String.format("maxPermits(%s) get from redis should be positive, Redis端口中%s的值可能还没有被初始化或是错误的值，请在Redis中手动重设该值或使用带maxPermits的构造方法", maxPermits, maxPermitsKey));
            resync();
        }
    }

    /**
     * 加载lua脚本
     */
    private void loadLuaScript() {
        String luaPath = "lua/rate_limit.lua";
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(luaPath);
        try {
            String script = new String(ByteStreams.toByteArray(resourceAsStream), Charsets.UTF_8);
            shaKey = jedis.scriptLoad(script);
        } catch (IOException e) {
            log.warn("loadLuaScript error, luaPath:{}", luaPath, e);
        }
    }

    /**
     * 开始监控redis的许可数的task，定时从redis申请许可
     * monitorPeriodInMillis由 1000 / monitorPeriodInMillis 计算得出
     */
    private void startMonitorTokenTask() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    int currentStoredPermits = storedPermits.get();
                    //当本地的许可数还没有消耗完时，不去从redis申请许可，避免远程许可过多地被本机无端消耗
                    if (currentStoredPermits > 0) {
                        log.debug("storedPermits is remaining, so not pre take token from redis, currentStoredPermits:{}", currentStoredPermits);
                        return;
                    }

                    int realIncrCount = applyTokenFromRedis();
                    if (realIncrCount > 0) {
                        //将redis申请来的许可加入本地存数的许可数里面
                        int addedStoredPermits = storedPermits.addAndGet(realIncrCount);
                        log.debug("applyTokenFromRedis return realIncrCount, realIncrCount:{}, addedStoredPermits:{}", realIncrCount, addedStoredPermits);
                        //通知所有等待的线程
                        synchronized (mutexWait) {
                            mutexWait.notifyAll();
                        }
                    } else {
                        log.debug("applyTokenFromRedis return realIncrCount <=0, realIncrCount:{}", realIncrCount);
                    }
                } catch (Exception e) {
                    log.warn("do monitorTokenTask error", e);
                }
            }
        }, monitorPeriodInMillis, monitorPeriodInMillis);
    }

    /**
     * 执行lua脚本，从redis申请许可
     * @return 申请到的许可数
     */
    private int applyTokenFromRedis() {
        try {
            int maxPermitsFromRedis = getMaxPermitsFromRedis();
            if (maxPermitsFromRedis != maxPermits) {
                int oldMaxPermits = maxPermits;
                resetMaxPermits(maxPermitsFromRedis);
                log.info("maxPermitsFromRedis is not equal to maxPermits, so resetMaxPermits. maxPermitsFromRedis:{}, oldMaxPermits:{}", maxPermitsFromRedis, oldMaxPermits);
            }
        } catch (Exception e) {
            log.warn("get and recheck maxPermits with redis error", e);
        }

        int realAppliedPermits;
        try {
            Long realIncrCount = (Long) jedis.evalsha(shaKey, 1, usedPermitsKey, Integer.toString(maxPermits), Integer.toString(applyPermitsPerCount));
            realAppliedPermits = realIncrCount.intValue();
        } catch (Exception e) {
            realAppliedPermits = defaultApplyPermitsPerCount;
            log.warn("applyTokenFromRedis error, so use default. usedPermitsKey:{}, maxPermits:{}, applyPermitsPerCount:{}", usedPermitsKey, maxPermits, applyPermitsPerCount, e);
        }
        return realAppliedPermits;
    }

    /** 缓存调用方与profileNodeName的map */
    private final Map<StackTraceElement, String> invokeStackTraceMap = new ConcurrentHashMap<>();

    /**
     * 请求1个许可
     * 等同于 acquire(1)
     * @return 许可等待时间 in millis
     */
    @Override
    public long acquire() {
        return acquire(1);
    }

    /**
     * 请求许可
     * @param permits 请求的许可数
     * @return 许可等待时间 in millis
     */
    @Override
    public long acquire(int permits) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        synchronized (mutexEntry) {
            int current = storedPermits.get();
            log.debug("acquire permits, permits:{}, current:{}", permits, current);
            //如果申请的许可数大于本地当前剩余许可数，则先获取剩余的许可，休息片刻后，再次从本地获取许可，直到所有许可都获取到
            while (permits > current) {
                permits -= current;
                storedPermits.addAndGet(-current);
                waitOnMutex();
                current = storedPermits.get();
            }
            //从本地许可数里面减去申请的许可
            storedPermits.addAndGet(-permits);
        }

        stopwatch.stop();
        long elapsedMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);

//        logProfile(permits, elapsedMillis);
        return elapsedMillis;
    }

    /** 打印profile日志 */
//    private void logProfile(int permits, long elapsedMillis) {
//        int stackPot = 3;
//        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
//        if (stackTrace.length > stackPot) {
//            String profileNodeName;
//            StackTraceElement stackTraceElement = stackTrace[stackPot];
//            if (invokeStackTraceMap.containsKey(stackTraceElement)) {
//                profileNodeName = invokeStackTraceMap.get(stackTraceElement);
//            } else {
//                String className = stackTraceElement.getClassName();
//                className = className.contains(".") ? className.substring(className.lastIndexOf(".") + 1) : className;
//                profileNodeName = className + "_" + stackTraceElement.getMethodName();
//                log.info("invokeStackTraceMap not contains key, so cal profileNodeName. stackTraceElement:{}, profileNodeName:{}", stackTraceElement, profileNodeName);
//                synchronized (invokeStackTraceMap) {
//                    String previousValue = invokeStackTraceMap.putIfAbsent(stackTraceElement, profileNodeName);
//                    if (!profileNodeName.equals(previousValue)) {
//                        log.info("invokeStackTraceMap still not contains key, so put profileNodeName to map. stackTraceElement:{}, profileNodeName:{}, previousValue:{}", stackTraceElement, profileNodeName, previousValue);
//                    }
//                }
//            }
//
//            ExtProfileUtil.accessStatisticOfService(profileNodeName, permits, elapsedMillis);
//        }
//    }

    /** 当前线程在mutex()上等待，最多等待1s */
    private void waitOnMutex() {
        synchronized (mutexWait) {
            try {
                mutexWait.wait(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
                log.warn("waitOnMutex error", e);
            }
        }
    }

    /** wait互斥锁对象 */
    private final Object mutexWait = new Object();
    /** entry互斥锁对象 */
    private final Object mutexEntry = new Object();

    /**
     * 重设全局最大许可的qps数
     * @param maxPermits 全局最大许可的qps数
     */
    @Override
    public void resetMaxPermits(int maxPermits) {
        this.maxPermits = maxPermits;
        resync();
    }

    /**
     * 重置一些关联属性值
     */
    private void resync() {
        applyCountsPerSecond = 1000 / monitorPeriodInMillis;
        applyPermitsPerCount = maxPermits / applyCountsPerSecond;
        defaultApplyPermitsPerCount = (maxPermits / defaultMcSize) / applyCountsPerSecond;
        log.info("resync done, monitorPeriodInMillis:{}, applyCountsPerSecond:{}, maxPermits:{}, applyPermitsPerCount:{}", monitorPeriodInMillis, applyCountsPerSecond, maxPermits, applyPermitsPerCount);
    }

    private String setMaxPermitsToRedis(int maxPermits) {
        return jedis.set(maxPermitsKey, Integer.toString(maxPermits));
    }

    private int getMaxPermitsFromRedis() throws Exception {
        String maxPermitsStr = jedis.get(maxPermitsKey);
        if (maxPermitsStr == null) {
            return 0;
        }
        return Integer.parseInt(maxPermitsStr);
    }

    public String getJedisAddress() {
        return jedisAddress;
    }

    public String getRateLimitKey() {
        return rateLimitKey;
    }

    public int getMaxPermits() {
        return maxPermits;
    }

    public int getDefaultMcSize() {
        return defaultMcSize;
    }

    public int getMonitorPeriodInMillis() {
        return monitorPeriodInMillis;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public int getApplyCountsPerSecond() {
        return applyCountsPerSecond;
    }

    public int getApplyPermitsPerCount() {
        return applyPermitsPerCount;
    }

    public int getDefaultApplyPermitsPerCount() {
        return defaultApplyPermitsPerCount;
    }

    public String getMaxPermitsKey() {
        return maxPermitsKey;
    }

    public String getUsedPermitsKey() {
        return usedPermitsKey;
    }

    public String getShaKey() {
        return shaKey;
    }

    public AtomicInteger getStoredPermits() {
        return storedPermits;
    }

    public void setMonitorPeriodInMillis(int monitorPeriodInMillis) {
        this.monitorPeriodInMillis = monitorPeriodInMillis;
        resync();
    }
}
