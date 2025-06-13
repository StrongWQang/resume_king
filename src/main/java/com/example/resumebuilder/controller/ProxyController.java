package com.example.resumebuilder.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @GetMapping("/image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam("url") String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            URLConnection connection = url.openConnection();
            
            // 设置请求头
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Referer", "https://web-oss-learn.oss-cn-beijing.aliyuncs.com");
            
            // 获取响应头
            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();
            
            // 读取图片数据
            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                
                byte[] imageData = out.toByteArray();
                
                // 设置响应头
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", contentType);
                headers.set("Content-Length", String.valueOf(contentLength));
                headers.set("Cache-Control", "public, max-age=31536000");
                headers.set("Access-Control-Allow-Origin", "*");
                
                return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            }
        } catch (IOException e) {
            logger.error("Error proxying image: {}", imageUrl, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 