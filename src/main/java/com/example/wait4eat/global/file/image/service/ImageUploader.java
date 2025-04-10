package com.example.wait4eat.global.file.image.service;

import com.example.wait4eat.global.file.image.dto.ImageUploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageUploader {

    ImageUploadResult uploadFile(MultipartFile file, String path);
    void deleteFile(String fileName);

    default List<ImageUploadResult> uploadFiles(List<MultipartFile> files, String path) {
        return files.stream()
                .map(file -> uploadFile(file, path))
                .toList();
    }
}
