# 分布式全局限速器

## 使用场景
 * 分布式服务需要全局限速的情况。如果只是单机限速，推荐使用`guava`的`com.google.common.util.concurrent.RateLimiter`。

## 使用方式

 ###示例：
  * spring中添加：
  ```
      <bean id="redisRateService" class="com.david.common.globalLimiter.impl.RedisRateServiceImpl">
          <!-- 限速基于redis，需要配置一个redis端口 -->
          <constructor-arg name="jedisAddress" value="${jedisAddress}"/>
          <!-- 配置限速使用的redis key，注意要在redis端口中保证唯一，否则相互影响 -->
          <constructor-arg name="rateLimitKey" value="${rateLimitKey}"/>
          <!-- 服务部署的节点数量，用于在redis资源有问题时使用 -->
          <constructor-arg name="defaultMcSize" value="${defaultMcSize}"/>
          <!-- qps限速值，每秒许可的最大值，最好设置为20的倍数，避免出现精度问题导致限速不准确 -->
          <constructor-arg name="maxPermits" value="${maxPermits}"/>
      </bean>
  ```
  * 代码中注入bean：
  ```
      @Resource
      private RedisRateService redisRateService;
  ```
  * 在需要限速的地方执行：
  ```
      redisRateService.acquire(); 
      或
      redisRateService.acquire(int permits);
  ```
  * 具体使用示例参见`com.weibo.api.harmonia.globalLimiter.test.RedisRateServiceTest`

 ###注意：
  * 如果需要更改要限速的值，则只需修改redis端口中`${rateLimitKey}_m`的值即可，立即生效。无需重新上线。
  
    * `${rateLimitKey}_m`的值具有永久性，下次上线时
      * 如果使用如下构造器，`maxPermits`会被设置为redis端口中`${rateLimitKey}_m`的值；
      ```
      public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize) {}
      ```
           
      * 如果使用构造器，`maxPermits`会使用注入进来的值，并重设redis端口中`${rateLimitKey}_m`的值。
      ```
      public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize, int maxPermits) {}
      ```
  * 所以：
    * 如果希望redis端口中的值永久生效，请使用
      ```
      public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize) {}
      ```
    * 如果希望每次上线重设redis端口中的值，请使用
      ```
      public RedisRateServiceImpl(String jedisAddress, String rateLimitKey, int defaultMcSize, int maxPermits) {}
      ```

## 原理
 * 本地使用一个线程定时去从redis端口请求许可，本地线程再从许可池中循环申请许可。


# 扩展的性能工具类ExtProfileUtil

## 使用场景
 * 一次请求需要统计多个qps的情况。

## 使用方式
 * 项目中使用如下方法即可，支持一次请求统计多个qps计数。
    ```
    ExtProfileUtil.accessStatisticOfService(String name);
    或
    ExtProfileUtil.accessStatisticOfService(String name, int count);
    ```
 * 详细示例请见`com.weibo.api.harmonia.globalLimiter.impl.RedisRateServiceImpl`的`private void logProfile(int permits, long elapsedMillis)`方法。
 
## 原理
 * 扩展了ProfileUtil类，支持了count参数

