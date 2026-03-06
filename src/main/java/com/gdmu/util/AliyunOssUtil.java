package com.gdmu.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.gdmu.config.AliyunOssConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class AliyunOssUtil {

    @Autowired
    private AliyunOssConfig aliyunOssConfig;

    /**
     * 上传文件到阿里云 OSS
     * @param file 要上传的文件
     * @param folder 存储文件夹
     * @return 上传后的文件 URL
     * @throws IOException IO 异常
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 创建 OSS 客户端
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOssConfig.getEndpoint(),
                aliyunOssConfig.getAccessKeyId(),
                aliyunOssConfig.getAccessKeySecret()
        );

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;
            String objectName = folder + "/" + fileName;

            // 上传文件
            ossClient.putObject(aliyunOssConfig.getBucketName(), objectName, file.getInputStream());

            // 生成文件 URL
            return "https://" + aliyunOssConfig.getBucketName() + "." + aliyunOssConfig.getEndpoint() + "/" + objectName;
        } finally {
            // 关闭 OSS 客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}