package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by 53254 on 2018/3/31 13:38 /mmal
 */
@Component
@Slf4j
public class RedissionManager {
    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port", "6379"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port", "6380"));
    private Config config = new Config();
    private Redisson redisson = null;

    @PostConstruct
    private void init() {
        try {
            config.useSingleServer().setAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());
            redisson = (Redisson) Redisson.create(config);
            log.info("初始化redission结束");
        } catch (Exception e) {
            log.error("redission init error", e);
        }
    }

    public Redisson getRedisson() {
        return redisson;
    }
}
