package com.atguigu.gmall.product.config;

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
    String endpointUrl;
    String accessKey;
    String secreKey;
    String bucketName;
}
