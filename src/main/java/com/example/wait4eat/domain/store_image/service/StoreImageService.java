package com.example.wait4eat.domain.store_image.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.store_image.dto.response.StoreImageResponse;
import com.example.wait4eat.domain.store_image.entity.StoreImage;
import com.example.wait4eat.domain.store_image.repository.StoreImageRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import com.example.wait4eat.global.file.consts.FilePath;
import com.example.wait4eat.global.file.image.dto.ImageUploadResult;
import com.example.wait4eat.global.file.image.service.ImageUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreImageService {

    private static final int MAX_IMAGES_PER_STORE = 5;

    private final StoreImageRepository storeImageRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ImageUploader imageUploader;

    @Transactional
    public List<StoreImageResponse> updateStoreImages(Long userId, Long storeId, List<MultipartFile> images) {
        User user = getUserById(userId);
        Store store = getStoreById(storeId);

        checkBeforeUploadStoreImages(user, store, images);

        List<ImageUploadResult> uploadResults = uploadImagesToStorage(store, images);
        return createStoreImages(store, uploadResults).stream()
                .map(StoreImageResponse::from)
                .toList();
    }

    @Transactional
    public void deleteStoreImage(Long userId, Long storeId, Long storeImageId) {
        User user = getUserById(userId);
        Store store = getStoreById(storeId);
        StoreImage storeImage = getStoreImageById(storeImageId);

        checkBeforeDeleteStoreImage(user, store, storeImage);

        deleteImageFromStorage(storeImage);
        deleteStoreImage(storeImage);
    }

    @Transactional(readOnly = true)
    public List<StoreImageResponse> getStoreImages(Long storeId) {
        Store store = getStoreById(storeId);
        return getStoreImagesByStore(store).stream()
                .map(StoreImageResponse::from)
                .toList();
    }

    private void checkBeforeUploadStoreImages(User user, Store store, List<MultipartFile> images) {
        validateStoreOwner(user, store);
        validateStoreImageUploadLimit(store, images.size());
    }

    private void checkBeforeDeleteStoreImage(User user, Store store, StoreImage storeImage) {
        validateStoreOwner(user, store);
        validateStoreImageBelongsToStore(store, storeImage);
    }

    private void validateStoreOwner(User user, Store store) {
        if (!store.getUser().equals(user)) {
            throw new CustomException(ExceptionType.NOT_OWNER_OF_STORE);
        }
    }

    private void validateStoreImageUploadLimit(Store store, int addingCount) {
        int existingCount = countStoreImagesByStore(store);
        if (existingCount + addingCount > MAX_IMAGES_PER_STORE) {
            throw new CustomException(ExceptionType.STORE_IMAGE_LIMIT_EXCEEDED);
        }
    }

    private void validateStoreImageBelongsToStore(Store store, StoreImage storeImage) {
        if (!storeImage.getStore().equals(store)) {
            throw new CustomException(ExceptionType.STORE_IMAGE_NOT_BELONGS_TO_STORE);
        }
    }

    private List<ImageUploadResult> uploadImagesToStorage(Store store, List<MultipartFile> images) {
        return imageUploader.uploadFiles(images, FilePath.STORE_IMAGE_FILE_PATH + store.getId() + "/");
    }

    private List<StoreImage> createStoreImages(Store store, List<ImageUploadResult> uploadResults) {
        List<StoreImage> storeImages = uploadResults.stream()
                .map(result -> StoreImage.builder()
                        .store(store)
                        .storedFileUrl(result.getStoredFileUrl())
                        .storedFileName(result.getStoredFileName())
                        .build()
                ).toList();
        storeImageRepository.saveAll(storeImages);
        return storeImages;
    }

    private void deleteImageFromStorage(StoreImage storeImage) {
        imageUploader.deleteFile(storeImage.getStoredFileName());
    }

    private void deleteStoreImage(StoreImage storeImage) {
        storeImageRepository.delete(storeImage);
    }

    private int countStoreImagesByStore(Store store) {
        return storeImageRepository.countDistinctByStore(store);
    }

    private List<StoreImage> getStoreImagesByStore(Store store) {
        return storeImageRepository.findAllByStore(store);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
    }

    private Store getStoreById(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));
    }

    private StoreImage getStoreImageById(Long storeImageId) {
        return storeImageRepository.findById(storeImageId)
                .orElseThrow(() -> new CustomException(ExceptionType.STORE_IMAGE_NOT_FOUND));
    }
}
