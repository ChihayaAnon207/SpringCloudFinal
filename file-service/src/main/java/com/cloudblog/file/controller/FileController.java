package com.cloudblog.file.controller;

import com.cloudblog.common.Result;
import com.cloudblog.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return Result.success(Map.of("url", url));
    }

    @GetMapping("/download/{filename}")
    public Result<Map<String, String>> getFile(@PathVariable String filename) {
        String url = fileService.getFileUrl(filename);
        return Result.success(Map.of("url", url));
    }
}
