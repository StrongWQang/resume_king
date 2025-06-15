package com.example.resumebuilder.service;

import com.example.resumebuilder.entity.CompanyLogo;
import com.example.resumebuilder.mapper.CompanyLogoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompanyLogoService {
    
    @Autowired
    private CompanyLogoMapper companyLogoMapper;

    public Map<String, Object> findByPage(String name, int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询数据
        List<CompanyLogo> records = companyLogoMapper.findByPage(name, offset, size);
        int total = companyLogoMapper.count(name);
        
        // 计算总页数
        int pages = (total + size - 1) / size;
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("pages", pages);
        result.put("current", page);
        result.put("size", size);
        result.put("records", records);
        
        return result;
    }
} 