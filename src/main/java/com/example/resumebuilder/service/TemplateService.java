package com.example.resumebuilder.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TemplateService {

    private static final String STORAGE_DIR = "storage/templates";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TemplateService() {
        try {
            Files.createDirectories(Paths.get(STORAGE_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public String saveTemplate(List<Map<String, Object>> templateData) {
        String id = UUID.randomUUID().toString();
        Path filePath = Paths.get(STORAGE_DIR, id + ".json");
        try {
            Files.writeString(filePath, objectMapper.writeValueAsString(templateData));
            return id;
        } catch (IOException e) {
            throw new RuntimeException("Could not save template", e);
        }
    }

    public List<Map<String, Object>> getTemplate(String id) {
        Path filePath = Paths.get(STORAGE_DIR, id + ".json");
        try {
            String content = Files.readString(filePath);
            return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Could not read template", e);
        }
    }

    public List<Map<String, Object>> exportTemplate(String id) {
        return getTemplate(id);
    }

    public List<Map<String, Object>> importTemplate(MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            return objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Could not import template", e);
        }
    }
} 