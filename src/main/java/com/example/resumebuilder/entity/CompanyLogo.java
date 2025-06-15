package com.example.resumebuilder.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanyLogo {
    private String id;
    private String name;
    private String url;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
} 