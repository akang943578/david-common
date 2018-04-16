package com.david.common.globalLimiter.test;

import com.google.common.base.Stopwatch;
import com.david.common.globalLimiter.RedisRateService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by jiakang on 2018/4/16
 *
 * @author jiakang
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:redis-rate-service-test.xml")
public class RedisRateServiceTest {

    @Resource
    private RedisRateService redisRateService;

    @Test
    public void testLimit() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (int i = 0; i < 30000; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    redisRateService.acquire();
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stopwatch.stop();
        log.info("testLimit, cost:{}", stopwatch);
    }

    @Test
    public void testLimitMulti() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (int i = 0; i < 30000; i++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    redisRateService.acquire(5);
                }
            };
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        stopwatch.stop();
        log.info("testLimitMulti, cost:{}", stopwatch);
    }
}
