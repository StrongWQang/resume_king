package com.example.resumebuilder.controller;

import com.example.resumebuilder.service.CompanyLogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/logo")
public class CompanyLogoController {

    @Autowired
    private CompanyLogoService companyLogoService;

    @GetMapping
    public Map<String, Object> getLogos(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size) {
        return companyLogoService.findByPage(name, page, size);
    }
} 