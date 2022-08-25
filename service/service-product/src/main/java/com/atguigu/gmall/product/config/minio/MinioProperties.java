package com.atguigu.gmall.product.config.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
    minio配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /*
      endpointUrl: http://192.168.6.152:9000
      accessKey: admin
      secreKey: admin123456
      bucketName: gmall
     */
    //以前的代码一个不改，以后的代码都能用
    //设计模式：   对新增开放，对修改关闭【开闭原则】
    String endpointUrl;
    String accessKey;
    String secreKey;
    String bucketName;
}
