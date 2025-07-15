package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.Resume;
import com.example.resumebuilder.service.ResumeService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume-square")
public class ResumeSquareController {

    @Resource
    ResumeService resumeService;

    @GetMapping("/templates")
    public ResponseEntity<?> getResumeTemplates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        if (page < 1) {
            return ResponseEntity.badRequest().body("页码必须大于0");
        }
        if (size < 1 || size > 50) {
            return ResponseEntity.badRequest().body("每页数量必须在1-50之间");
        }
        return ResponseEntity.ok(resumeService.getTemplatesWithPagination(page, size));
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<Resume>> getPopularResumes(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(resumeService.getPopularResumes(limit));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeResume(@PathVariable String id) {
        resumeService.increaseLikeCount(Long.valueOf(id));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeResume(@PathVariable String id) {
        resumeService.decreaseLikeCount(Long.valueOf(id));
        return ResponseEntity.ok().build();
    }
}
