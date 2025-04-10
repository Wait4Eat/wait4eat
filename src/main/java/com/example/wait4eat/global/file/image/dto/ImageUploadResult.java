package com.example.wait4eat.global.file.image.dto;

import lombok.Getter;

@Getter
public class ImageUploadResult {

    private final String storedFileName;
    private final String storedFileUrl;

    public ImageUploadResult(String storedFileName, String storedFileUrl) {
        this.storedFileName = storedFileName;
        this.storedFileUrl = storedFileUrl;
    }
}
