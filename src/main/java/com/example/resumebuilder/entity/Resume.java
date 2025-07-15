package com.example.resumebuilder.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Resume {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String userId;
    private Boolean isTemplate;
    private Integer likeCount;
    private Integer status;
    
    // 状态常量
    public static final int STATUS_DRAFT = 0;    // 草稿
    public static final int STATUS_PUBLISHED = 1; // 发布
    public static final int STATUS_ARCHIVED = 2;  // 归档
    public static final int STATUS_DELETED = 3;   // 删除

    // 获取String类型的ID，用于前端展示
    public String getIdString() {
        return id != null ? String.valueOf(id) : null;
    }
} 