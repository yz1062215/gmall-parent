package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传
 */
@RequestMapping("/admin/product")
@RestController
public class FileUploadController {


    /**
     * 文件上传功能
     * 1、前端把文件流放到哪里了？我们该怎么拿到？
     *     Post请求数据在请求体（包含了文件[流]）
     * 如何接：
     * @RequestParam("file")MultipartFile file
     * @RequestPart("file")MultipartFile file: 专门处理文件的
     *
     * 各种注解接不通位置的请求数据
     * @RequestParam: 无论是什么请求 接请求参数； 用一个Pojo把所有数据都接了
     * @RequestPart： 接请求参数里面的文件项
     * @RequestBody： 接请求体中的所有数据 (json转为pojo)
     * @PathVariable: 接路径上的动态变量
     * @RequestHeader: 获取浏览器发送的请求的请求头中的某些值
     * @CookieValue： 获取浏览器发送的请求的Cookie值
     * - 如果多个就写数据，否则就写单个对象
     *
     *
     * @return
     */
    //http://192.168.6.1/admin/product/fileUpload
    @Autowired
    MinioProperties minioProperties;

    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception  {
        //TODO 品牌图片上传功能
        String url = UploadFile(file);
        return Result.ok(url);
    }

    private String UploadFile(MultipartFile file) throws Exception
    {
        String url="";
        //创建minioClient对象  构建minio对象传入图片上传地址一级ak 和 sk
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpointUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecreKey())
                .build();
        //判断图片桶是否存在
        boolean b = minioClient
                .bucketExists(BucketExistsArgs.builder().
                        bucket(minioProperties.getBucketName())
                        .build());
        if (!b){
            //不存在
            //创建新桶
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
        }
        //定义一个随机文件名
        String fileName=System.currentTimeMillis()+ UUID.randomUUID().toString();
        //上传文件到桶中
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(fileName)
                .stream(file.getInputStream(), file.getSize(), -1).
                        contentType(file.getContentType())
                .build());
        //获取上传成功后的文件名  minio服务器地址+同名+随机生成的文件名
        url=minioProperties.getEndpointUrl()+"/"+minioProperties.getBucketName()+"/"+fileName;
        System.out.println("url = " + url);
        return url;
    }

    //前端传参练习
    @PostMapping("/reg")
    public Result reg(String username,
                      @RequestParam("password")String password,
                      @RequestParam("email")String email,
                      @RequestPart("header") MultipartFile header,
                      @RequestPart("sfz")MultipartFile sfz,
                      @RequestPart("shz")MultipartFile shz,
                      @RequestParam("ah")String[] ah,
                      @RequestHeader("Cache-Control") String cache,
                      @CookieValue("jsessionid") String jsessionid){
        //1、用户名，密码，邮箱
        Map<String,Object> result = new HashMap<>();
        result.put("用户名：",username);
        result.put("密码：",password);
        result.put("邮箱：",email);

        //头像
        result.put("头像文件大小", header.getSize());
        result.put("生活照文件大小？",sfz.getSize());
        result.put("身份证文件大小？",shz.getSize());
        result.put("爱好", Arrays.asList(ah));
        result.put("cache",cache);
        return Result.ok(result);
    }

}
