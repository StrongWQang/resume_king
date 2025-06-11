package com.example.resumebuilder.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResumeService {

    private static final String STORAGE_DIR = "storage/resumes";

    public ResumeService() {
        // 确保存储目录存在
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("无法创建存储目录", e);
        }
    }

    public String saveResume(List<Map<String, Object>> resumeData) {
        String id = UUID.randomUUID().toString();
        Path filePath = Paths.get(STORAGE_DIR, id + ".json");
        
        try {
            Files.writeString(filePath, resumeData.toString());
            return id;
        } catch (IOException e) {
            throw new RuntimeException("保存简历失败", e);
        }
    }

    public List<Map<String, Object>> getResume(String id) {
        Path filePath = Paths.get(STORAGE_DIR, id + ".json");
        
        try {
            String content = Files.readString(filePath);
            // TODO: 将 JSON 字符串转换为 List<Map<String, Object>>
            return List.of(); // 临时返回空列表
        } catch (IOException e) {
            throw new RuntimeException("读取简历失败", e);
        }
    }

    public byte[] generatePdf(List<Map<String, Object>> resumeData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 遍历简历数据并添加到 PDF
            for (Map<String, Object> component : resumeData) {
                String type = (String) component.get("type");
                if ("text".equals(type)) {
                    String content = (String) component.get("content");
                    if (content != null) {
                        Paragraph paragraph = new Paragraph(content);
                        document.add(paragraph);
                    }
                }
                // TODO: 处理其他类型的组件
            }

            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成 PDF 失败", e);
        }
    }
} 