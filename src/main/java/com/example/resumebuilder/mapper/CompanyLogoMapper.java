package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.CompanyLogo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CompanyLogoMapper {
    List<CompanyLogo> findByPage(@Param("name") String name, 
                                @Param("offset") int offset, 
                                @Param("size") int size);
    
    int count(@Param("name") String name);
} 