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
import com.itextpdf.layout.element.Image; // Added import
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.io.image.ImageData; // Added import
import com.itextpdf.io.image.ImageDataFactory; // Added import
import com.itextpdf.kernel.pdf.canvas.PdfCanvas; // Added import

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL; // Added import

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
            
            // 设置页面大小为A4
            pdf.setDefaultPageSize(com.itextpdf.kernel.geom.PageSize.A4);
            
            // 创建第一页
            pdf.addNewPage();
            
            Document document = new Document(pdf);

            // 尝试加载中文字体，如果失败则使用默认字体
            PdfFont font;
            try {
                font = PdfFontFactory.createFont("STSongStd-Light", PdfEncodings.IDENTITY_H);
            } catch (Exception e) {
                logger.warn("无法加载中文字体，使用默认字体: {}", e.getMessage());
                font = PdfFontFactory.createFont();
            }
            document.setFont(font);

            // 获取页面尺寸
            float pageWidth = pdf.getDefaultPageSize().getWidth();
            float pageHeight = pdf.getDefaultPageSize().getHeight();

            // 处理组件
            for (Map<String, Object> component : resumeData) {
                try {
                    String type = (String) component.get("type");
                    if (type == null) {
                        logger.warn("组件类型为空，跳过处理");
                        continue;
                    }

                    Double x = ((Number) component.get("x")).doubleValue();
                    Double y = ((Number) component.get("y")).doubleValue();
                    Double width = ((Number) component.get("width")).doubleValue();
                    Double height = ((Number) component.get("height")).doubleValue();
                    String color = (String) component.get("color");

                    // 确保坐标和尺寸在有效范围内
                    x = Math.max(0, Math.min(x, pageWidth));
                    y = Math.max(0, Math.min(y, pageHeight));
                    width = Math.min(width, pageWidth - x);
                    height = Math.min(height, pageHeight - y);

                    // 转换Y坐标（从顶部到底部）
                    float pdfY = pageHeight - y.floatValue() - height.floatValue();

                    DeviceRgb componentColor = null;
                    if (color != null && color.startsWith("#") && color.length() == 7) {
                        try {
                            String hexColor = color.substring(1);
                            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
                            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
                            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
                            componentColor = new DeviceRgb(r, g, b);
                        } catch (NumberFormatException e) {
                            logger.warn("无效的颜色格式: {}", color);
                        }
                    }

                    if ("text-title".equals(type) || "text-basic".equals(type)) {
                        String content = (String) component.get("content");
                        if (content == null) {
                            logger.warn("文本内容为空，跳过处理");
                            continue;
                        }

                        Double fontSize = component.get("fontSize") != null ?
                                ((Number) component.get("fontSize")).doubleValue() : 12.0;
                        String textAlign = (String) component.get("textAlign");
                        Double lineHeight = component.get("lineHeight") != null ?
                                ((Number) component.get("lineHeight")).doubleValue() : 1.5;

                        Paragraph paragraph = new Paragraph(content);
                        paragraph.setFont(font);
                        paragraph.setFontSize(fontSize.floatValue());

                        if (componentColor != null) {
                            paragraph.setFontColor(componentColor);
                        }

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
                        paragraph.setMultipliedLeading(lineHeight.floatValue());
                        paragraph.setMargin(0);

                        Div positionedDiv = new Div();
                        positionedDiv.setFixedPosition(x.floatValue(), pdfY, width.floatValue());
                        positionedDiv.setHeight(UnitValue.createPointValue(height.floatValue()));
                        positionedDiv.add(paragraph);
                        document.add(positionedDiv);
                    } else if ("image".equals(type)) {
                        String imageUrl = (String) component.get("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            try {
                                ImageData imageData = ImageDataFactory.create(new URL(imageUrl));
                                Image image = new Image(imageData);
                                image.setFixedPosition(x.floatValue(), pdfY, width.floatValue());
                                image.setHeight(UnitValue.createPointValue(height.floatValue()));
                                document.add(image);
                            } catch (Exception e) {
                                logger.error("加载图片失败: {}", imageUrl, e);
                                // 添加一个占位符文本
                                Paragraph placeholder = new Paragraph("[图片加载失败]");
                                placeholder.setFont(font);
                                placeholder.setFontSize(12);
                                Div positionedDiv = new Div();
                                positionedDiv.setFixedPosition(x.floatValue(), pdfY, width.floatValue());
                                positionedDiv.setHeight(UnitValue.createPointValue(height.floatValue()));
                                positionedDiv.add(placeholder);
                                document.add(positionedDiv);
                            }
                        }
                    } else if ("divider-solid".equals(type)) {
                        Double thickness = component.get("thickness") != null ?
                                ((Number) component.get("thickness")).doubleValue() : 1.0;
                        Double padding = component.get("padding") != null ?
                                ((Number) component.get("padding")).doubleValue() : 0.0;

                        float lineY = pageHeight - (y.floatValue() + height.floatValue() / 2);

                        PdfCanvas canvas = new PdfCanvas(pdf.getFirstPage());
                        canvas.setStrokeColor(componentColor != null ? componentColor : new DeviceRgb(0, 0, 0));
                        canvas.setLineWidth(thickness.floatValue());
                        canvas.moveTo(x.floatValue() + padding.floatValue(), lineY);
                        canvas.lineTo(x.floatValue() + width.floatValue() - padding.floatValue(), lineY);
                        canvas.stroke();
                    }
                } catch (Exception e) {
                    logger.error("处理组件时出错: {}", e.getMessage(), e);
                    // 继续处理下一个组件
                }
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }
}

