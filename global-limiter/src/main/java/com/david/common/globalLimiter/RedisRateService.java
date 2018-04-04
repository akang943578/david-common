package com.david.common.globalLimiter;

/**
 * Created by jiakang on 2017/12/5.
 * 基于Redis的分布式限速服务
 * @author jiakang
 */
public interface RedisRateService {

    /**
     * 请求1个许可
     * 等同于 acquire(1)
     * @return 许可等待时间 in millis
     */
    long acquire();

    /**
     * 请求许可
     * @param permits 请求的许可数
     * @return 许可等待时间 in millis
     */
    long acquire(int permits);

    /**
     * 重设全局最大许可的qps数
     * @param maxPermits 全局最大许可的qps数
     */
    void resetMaxPermits(int maxPermits);
}
