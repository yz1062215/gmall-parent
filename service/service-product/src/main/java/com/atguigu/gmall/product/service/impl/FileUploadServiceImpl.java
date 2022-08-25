package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.product.config.minio.MinioProperties;
import com.atguigu.gmall.product.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    MinioProperties minioProperties;
    @Autowired
    MinioClient minioClient;
    @Override
    public String fileUpload(MultipartFile file) {
        try {
            ////创建客户端
            //MinioClient minioClient = new MinioClient(
            //        minioProperties.getEndpointUrl(),
            //        minioProperties.getAccessKey(),
            //        minioProperties.getSecreKey());
            //
            ////判断桶是否存在
            //if (!minioClient.bucketExists(minioProperties.getBucketName())){
            //    //不存在 创建新桶
            //    minioClient.makeBucket(minioProperties.getBucketName());
            //}
            //
            String dateStr = DateUtil.formatDate(new Date());
            String fileName = UUID.randomUUID().
                    toString().replace("-", "") +
                    "_" + file.getOriginalFilename(); //原始文件名

            //TODO 上传......
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            //文件上传参数：long objectSize, long partSize
            PutObjectOptions options = new PutObjectOptions(inputStream.available(),-1L);
            options.setContentType(contentType);
            minioClient.putObject(
                    minioProperties.getBucketName(),
                    dateStr+"/"+fileName,//自己指定的唯一名
                    inputStream,
                    options);
            String url=minioProperties.getEndpointUrl()+"/"+minioProperties.getBucketName()+"/"+dateStr+"/"+fileName;
            System.out.println("url = " + url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
