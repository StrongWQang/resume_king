package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.Resume;
import com.example.resumebuilder.mapper.ResumeMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Div;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.DeviceRgb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ResumeService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeService.class);

    @Autowired
    private ResumeMapper resumeMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public String saveResume(List<Map<String, Object>> resumeData) {
        try {
            logger.info("开始保存简历数据: {}", resumeData);
            
            String id = UUID.randomUUID().toString();
            String content = objectMapper.writeValueAsString(resumeData);
            logger.debug("转换后的JSON内容: {}", content);
            
            // 创建简历实体
            Resume resume = new Resume();
            resume.setId(id);
            resume.setContent(content);
            resume.setCreateTime(LocalDateTime.now());
            
            // 保存到数据库
            resumeMapper.insert(resume);
            logger.info("简历保存成功，ID: {}", id);
            
            return id;
        } catch (Exception e) {
            logger.error("保存简历失败", e);
            throw new RuntimeException("保存简历失败: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getResume(String id) {
        try {
            logger.info("开始获取简历数据，ID: {}", id);
            
            Resume resume = resumeMapper.findById(id);
            if (resume == null) {
                logger.warn("简历不存在，ID: {}", id);
                throw new RuntimeException("简历不存在");
            }
            
            List<Map<String, Object>> result = objectMapper.readValue(resume.getContent(), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
            logger.info("成功获取简历数据，ID: {}", id);
            
            return result;
        } catch (Exception e) {
            logger.error("读取简历失败", e);
            throw new RuntimeException("读取简历失败: " + e.getMessage(), e);
        }
    }

    public List<Resume> getAllResumes() {
        logger.info("获取所有简历列表");
        return resumeMapper.findAll();
    }

    @Transactional
    public void deleteResume(String id) {
        try {
            logger.info("开始删除简历，ID: {}", id);
            resumeMapper.deleteById(id);
            logger.info("简历删除成功，ID: {}", id);
        } catch (Exception e) {
            logger.error("删除简历失败", e);
            throw new RuntimeException("删除简历失败: " + e.getMessage(), e);
        }
    }

    public byte[] generatePdf(List<Map<String, Object>> resumeData) {

        logger.info("开始生成PDF，接收到的简历数据：{}", resumeData.toString());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // 设置页面大小为A4
            pdf.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);
            
            // 加载中文字体
            PdfFont font = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
            document.setFont(font);
            
            // 创建主容器
            Div mainContainer = new Div();
            mainContainer.setWidth(UnitValue.createPercentValue(100));
            mainContainer.setHeight(UnitValue.createPercentValue(100));
            
            // 遍历组件并添加到PDF
            for (Map<String, Object> component : resumeData) {
                String type = (String) component.get("type");
                String content = (String) component.get("content");
                Double x = ((Number) component.get("x")).doubleValue();
                Double y = ((Number) component.get("y")).doubleValue();
                Double width = ((Number) component.get("width")).doubleValue();
                Double height = ((Number) component.get("height")).doubleValue();
                Double fontSize = component.get("fontSize") != null ? 
                    ((Number) component.get("fontSize")).doubleValue() : 12.0;
                String color = (String) component.get("color");
                String textAlign = (String) component.get("textAlign");
                Double lineHeight = component.get("lineHeight") != null ? 
                    ((Number) component.get("lineHeight")).doubleValue() : 1.5;
                
                if (content != null) {
                    Paragraph paragraph = new Paragraph(content);
                    paragraph.setFont(font);  // 确保每个段落都使用中文字体
                    
                    // 设置字体大小
                    paragraph.setFontSize(fontSize.floatValue());
                    
                    // 设置颜色
                    if (color != null) {
                        String[] rgb = color.replace("#", "").split("(?<=\\G.{2})");
                        paragraph.setFontColor(new DeviceRgb(
                            Integer.parseInt(rgb[0], 16),
                            Integer.parseInt(rgb[1], 16),
                            Integer.parseInt(rgb[2], 16)
                        ));
                    }
                    
                    // 设置对齐方式
                    if (textAlign != null) {
                        switch (textAlign) {
                            case "center":
                                paragraph.setTextAlignment(TextAlignment.CENTER);
                                break;
                            case "right":
                                paragraph.setTextAlignment(TextAlignment.RIGHT);
                                break;
                            default:
                                paragraph.setTextAlignment(TextAlignment.LEFT);
                        }
                    }
                    
                    // 设置行高
                    paragraph.setMultipliedLeading(lineHeight.floatValue());
                    
                    // 创建定位容器
                    Div positionedDiv = new Div();
                    positionedDiv.setFixedPosition(x.floatValue(), y.floatValue(), width.floatValue());
                    positionedDiv.setHeight(UnitValue.createPointValue(height.floatValue()));
                    positionedDiv.add(paragraph);
                    
                    mainContainer.add(positionedDiv);
                }
            }
            
            document.add(mainContainer);
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }
} 