package com.example.resumebuilder.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileName = UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
        String objectName = "resume/images/" + fileName;

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file.getInputStream());
            
            // 上传文件
            ossClient.putObject(putObjectRequest);
            
            // 生成访问URL
            String url = urlPrefix + objectName;
            
            logger.info("Successfully uploaded file: {} to OSS", objectName);
            
            return ResponseEntity.ok(Map.of(
                "url", url,
                "fileName", fileName,
                "objectName", objectName
            ));
        } catch (OSSException e) {
            logger.error("OSS error occurred while uploading file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "OSS错误: " + e.getMessage(),
                "errorCode", e.getErrorCode(),
                "requestId", e.getRequestId()
            ));
        } catch (IOException e) {
            logger.error("IO error occurred while uploading file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "文件上传失败: " + e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error occurred while uploading file: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "上传失败: " + e.getMessage()
            ));
        } finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDotIndex = fileName.lastIndexOf(".");
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }
} 