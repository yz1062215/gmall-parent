package com.atguigu.gmall.common.config.threadpool;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.thread-pool")
@Data
public class AppThreadPoolProperties {
    /*
        core: 4
    max: 8
    queue-size: 2000
    keep-alive-time: 300
     */
    Integer core =2;
    Integer max=4;
    Integer queueSize=200;
    Long keepAliveTime=300L;
}
