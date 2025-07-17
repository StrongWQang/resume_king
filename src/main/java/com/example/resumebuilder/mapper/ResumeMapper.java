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
    Resume findById(Long id);
    
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
    void deleteById(Long id);
    
    @Update("UPDATE resume SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") int status);

    @Update("UPDATE resume SET like_count = like_count + 1 WHERE id = #{id}")
    void increaseLikeCount(Long id);

    @Update("UPDATE resume SET like_count = like_count - 1 WHERE id = #{id} AND like_count > 0")
    void decreaseLikeCount(Long id);
    
    @Update("UPDATE resume SET content = #{content}, update_time = #{updateTime}, title = #{title}, is_template = #{isTemplate}, status = #{status} WHERE id = #{id}")
    void updateResume(Resume resume);

    @Update("<script>" +
            "UPDATE resume SET " +
            "<trim prefixOverrides=','>" +
            "<if test='content != null'>, content = #{content}</if>" +
            "<if test='updateTime != null'>, update_time = #{updateTime}</if>" +
            "<if test='title != null'>, title = #{title}</if>" +
            "<if test='status != null'>, status = #{status}</if>" +
            "<if test='isTemplate != null'>, is_template = #{isTemplate}</if>" +
            "<if test='likeCount != null'>, like_count = #{likeCount}</if>" +
            "</trim>" +
            "WHERE id = #{id}" +
            "</script>")
    void update(Resume resume);
}