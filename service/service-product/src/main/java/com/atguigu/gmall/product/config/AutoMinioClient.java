package com.atguigu.gmall.product.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AutoMinioClient {
    @Autowired
    MinioProperties minioProperties;
    @Bean
    public MinioClient minioClient() throws Exception {
        //创建minio客户端
        MinioClient minioClient = new MinioClient(
                minioProperties.endpointUrl,
                minioProperties.getAccessKey(),
                minioProperties.getSecreKey());
        if (!minioClient.bucketExists(minioProperties.getBucketName())){
            //如果桶名不存在创建新桶
            minioClient.makeBucket(minioProperties.getBucketName());
        }
        return minioClient;
    }
}
