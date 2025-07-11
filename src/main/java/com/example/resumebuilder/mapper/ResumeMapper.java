package com.example.resumebuilder.mapper;

import com.example.resumebuilder.entity.Resume;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ResumeMapper {
    
    @Insert("INSERT INTO resume (id, content, create_time, update_time, status, like_count, user_id, title, is_template) " +
            "VALUES (#{id}, #{content}, #{createTime}, #{updateTime}, #{status}, #{likeCount}, #{userId}, #{title}, #{isTemplate})")
    void insert(Resume resume);
    
    @Select("SELECT * FROM resume WHERE id = #{id} AND status != 3")
    Resume findById(String id);
    
    @Select("SELECT * FROM resume WHERE status = 1 ORDER BY create_time DESC")
    List<Resume> findAll();
    
    @Select("SELECT * FROM resume WHERE status = 1 AND is_template = 1 ORDER BY create_time DESC")
    List<Resume> findAllTemplates();
    
    @Select("SELECT * FROM resume WHERE status = 1 AND is_template = 1 ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Resume> findTemplatesPaginated(@Param("offset") int offset, @Param("limit") int limit);
    
    @Select("SELECT COUNT(*) FROM resume WHERE status = 1 AND is_template = 1")
    int countTemplates();
    
    @Select("SELECT * FROM resume WHERE status = 1 ORDER BY like_count DESC LIMIT #{limit}")
    List<Resume> findPopular(int limit);
    
    @Select("SELECT * FROM resume WHERE user_id = #{userId} AND status != 3 ORDER BY create_time DESC")
    List<Resume> findByUserId(String userId);
    
    @Update("UPDATE resume SET status = 3 WHERE id = #{id}")
    void deleteById(String id);
    
    @Update("UPDATE resume SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") String id, @Param("status") int status);

    @Update("UPDATE resume SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(String id);

    @Update("UPDATE resume SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decreaseLikeCount(String id);
    
    @Update("UPDATE resume SET content = #{content}, update_time = #{updateTime}, title = #{title} WHERE id = #{id}")
    void updateResume(Resume resume);
}