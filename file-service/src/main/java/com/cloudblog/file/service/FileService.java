package com.cloudblog.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String upload(MultipartFile file);
    String getFileUrl(String filename);
}
