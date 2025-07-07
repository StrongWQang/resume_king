package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.Resume;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResumeMapper {
    
    @Insert("INSERT INTO resume (id, content, create_time) VALUES (#{id}, #{content}, #{createTime})")
    void insert(Resume resume);
    
    @Select("SELECT * FROM resume WHERE id = #{id}")
    Resume findById(String id);
    
    @Select("SELECT * FROM resume ORDER BY create_time DESC")
    List<Resume> findAll();
    
    @Delete("DELETE FROM resume WHERE id = #{id}")
    void deleteById(String id);

    @Update("UPDATE resume SET `like` = `like` + 1 WHERE id = #{id}")
    void increaseLikeCount(String id);

    @Update("UPDATE resume SET `like` = `like` - 1 WHERE id = #{id} AND `like` > 0")
    void decreaseLikeCount(String id);
}