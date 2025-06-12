package com.example.resumebuilder.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResumeService {

    private static final String STORAGE_DIR = System.getProperty("user.home") + "/resume_king/storage/resumes";
    private final ObjectMapper objectMapper;
    private static final Map<String, String> FONT_MAPPING = new HashMap<>();
    private static final float SCALE_FACTOR = 0.75f; // 缩放因子，用于调整 PDF 中的尺寸

    static {
        // 字体映射
        FONT_MAPPING.put("Microsoft YaHei", "STSong-Light");
        FONT_MAPPING.put("SimSun", "STSong-Light");
        FONT_MAPPING.put("SimHei", "STHeiti-Light");
        FONT_MAPPING.put("KaiTi", "STKaiti");
        FONT_MAPPING.put("FangSong", "STFangsong");
    }

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public ResumeService() {
        this.objectMapper = new ObjectMapper();
        // 确保存储目录存在
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("无法创建存储目录", e);
        }
    }

    public String saveResume(List<Map<String, Object>> resumeData) {
        String id = UUID.randomUUID().toString();
        String objectName = "resumes/" + id + ".json";
        
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        
        try {
            // 将数据转换为JSON字符串
            String jsonContent = objectMapper.writeValueAsString(resumeData);
            
            // 上传到OSS
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8)));
            
            // 同时保存到本地（作为备份）
            Path filePath = Paths.get(STORAGE_DIR, id + ".json");
            Files.writeString(filePath, jsonContent);
            
            return id;
        } catch (IOException e) {
            throw new RuntimeException("保存简历失败", e);
        } finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }
    }

    public List<Map<String, Object>> getResume(String id) {
        String objectName = "resumes/" + id + ".json";
        
        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        
        try {
            // 从OSS获取文件
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            
            // 读取文件内容
            String content = new String(ossObject.getObjectContent().readAllBytes(), StandardCharsets.UTF_8);
            
            // 解析JSON
            return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
        } catch (OSSException e) {
            // 如果OSS中不存在，尝试从本地读取
            Path filePath = Paths.get(STORAGE_DIR, id + ".json");
            try {
                if (Files.exists(filePath)) {
                    String content = Files.readString(filePath);
                    return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
                }
            } catch (IOException ex) {
                throw new RuntimeException("读取本地简历失败", ex);
            }
            throw new RuntimeException("简历不存在");
        } catch (IOException e) {
            throw new RuntimeException("读取简历失败", e);
        } finally {
            // 关闭OSSClient
            ossClient.shutdown();
        }
    }

    public byte[] generatePdf(List<Map<String, Object>> resumeData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // 创建 PDF 文档
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            
            // 遍历所有组件
            for (Map<String, Object> component : resumeData) {
                String type = (String) component.get("type");
                float x = ((Number) component.get("x")).floatValue() * SCALE_FACTOR;
                float y = ((Number) component.get("y")).floatValue() * SCALE_FACTOR;
                float width = ((Number) component.get("width")).floatValue() * SCALE_FACTOR;
                float height = ((Number) component.get("height")).floatValue() * SCALE_FACTOR;
                
                // 根据组件类型处理
                if ("text".equals(type)) {
                    String content = (String) component.get("content");
                    String fontFamily = (String) component.get("fontFamily");
                    Number fontSize = (Number) component.get("fontSize");
                    String color = (String) component.get("color");
                    String textAlign = (String) component.get("textAlign");
                    
                    // 创建段落
                    Paragraph paragraph = new Paragraph(content);
                    
                    // 设置字体
                    String pdfFontName = FONT_MAPPING.getOrDefault(fontFamily, "STSong-Light");
                    PdfFont font = PdfFontFactory.createFont(pdfFontName, "UniGB-UCS2-H");
                    paragraph.setFont(font);
                    
                    // 设置字体大小
                    float pdfFontSize = fontSize != null ? fontSize.floatValue() * SCALE_FACTOR : 12;
                    paragraph.setFontSize(pdfFontSize);
                    
                    // 设置颜色
                    if (color != null) {
                        try {
                            Color awtColor = Color.decode(color);
                            DeviceRgb pdfColor = new DeviceRgb(
                                awtColor.getRed() / 255f,
                                awtColor.getGreen() / 255f,
                                awtColor.getBlue() / 255f
                            );
                            paragraph.setFontColor(pdfColor);
                        } catch (Exception e) {
                            paragraph.setFontColor(ColorConstants.BLACK);
                        }
                    }
                    
                    // 设置对齐方式
                    if (textAlign != null) {
                        switch (textAlign.toLowerCase()) {
                            case "center":
                                paragraph.setTextAlignment(TextAlignment.CENTER);
                                break;
                            case "right":
                                paragraph.setTextAlignment(TextAlignment.RIGHT);
                                break;
                            case "justify":
                                paragraph.setTextAlignment(TextAlignment.JUSTIFIED);
                                break;
                            default:
                                paragraph.setTextAlignment(TextAlignment.LEFT);
                        }
                    }
                    
                    // 设置位置和大小
                    paragraph.setFixedPosition(x, PageSize.A4.getHeight() - y - height, width);
                    document.add(paragraph);
                    
                } else if ("image".equals(type)) {
                    String imageUrl = (String) component.get("imageUrl");
                    if (imageUrl != null) {
                        try {
                            // 从 URL 加载图片数据
                            byte[] imageBytes = new URL(imageUrl).openStream().readAllBytes();
                            ImageData imageData = ImageDataFactory.create(imageBytes);
                            
                            // 创建图片
                            Image image = new Image(imageData);
                            
                            // 设置位置和大小
                            image.setFixedPosition(x, PageSize.A4.getHeight() - y - height);
                            image.setWidth(UnitValue.createPointValue(width));
                            image.setHeight(UnitValue.createPointValue(height));
                            
                            // 设置图片缩放模式
                            String objectFit = (String) component.get("objectFit");
                            if ("contain".equals(objectFit)) {
                                image.setAutoScale(true);
                            }
                            
                            document.add(image);
                        } catch (IOException e) {
                            System.err.println("加载图片失败: " + imageUrl);
                        }
                    }
                }
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("生成PDF失败", e);
        }
    }
} 