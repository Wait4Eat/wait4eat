package com.example.wait4eat.infra.s3.service;

import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import com.example.wait4eat.global.file.image.dto.ImageUploadResult;
import com.example.wait4eat.global.file.image.service.ImageUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageUploader implements ImageUploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Override
    public ImageUploadResult uploadFile(MultipartFile file, String path) {
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = generateUniqueFileName(originalFilename);
        String storedFileName = path + uniqueFileName;

        log.info("[UPLOAD INIT] File: {}, Stored As: {}", originalFilename, storedFileName);

        try {
            uploadToS3(file, storedFileName);
            return new ImageUploadResult(storedFileName, getFileUrl(storedFileName));
        } catch (IOException e) {
            log.error("[UPLOAD FAILED] IOException for file: {}, Error: {}", storedFileName, e.getMessage());
            throw new CustomException(ExceptionType.FILE_UPLOAD_FAILED);
        } catch (S3Exception e) {
            log.error("[UPLOAD FAILED] S3Exception for file: {}, Error: {}", storedFileName, e.awsErrorDetails().errorMessage());
            throw new CustomException(ExceptionType.FILE_UPLOAD_FAILED);
        }
    }

    private void uploadToS3(MultipartFile file, String fileName) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .metadata(metadata)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
    }

    @Override
    public void deleteFile(String fileName) {
        log.info("[DELETE INIT] Trying to delete: {}", fileName);

        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build()
            );
        } catch (S3Exception e) {
            log.error("[DELETE FAILED] S3Exception for file: {}, Error: {}", fileName, e.awsErrorDetails().errorMessage());
            throw new CustomException(ExceptionType.FILE_DELETE_FAILED);
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        if(!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        return UUID.randomUUID() + "_" + originalFilename;
    }

    private String getFileUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                fileName);
    }
}
