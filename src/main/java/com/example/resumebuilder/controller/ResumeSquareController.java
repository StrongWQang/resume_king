package com.example.resumebuilder.controller;

import com.example.resumebuilder.entity.Resume;
import com.example.resumebuilder.service.ResumeService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeSquareController {

    @Resource
    ResumeService resumeService;

    @GetMapping("/templates")
    public ResponseEntity<List<Resume>> getResumeTemplates() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> likeResume(@PathVariable String id) {
        resumeService.increaseLikeCount(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlikeResume(@PathVariable String id) {
        resumeService.decreaseLikeCount(id);
        return ResponseEntity.ok().build();
    }
}
