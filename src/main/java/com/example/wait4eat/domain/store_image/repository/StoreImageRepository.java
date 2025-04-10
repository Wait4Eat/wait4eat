package com.example.wait4eat.domain.store_image.repository;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store_image.entity.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {

    int countDistinctByStore(Store store);
    List<StoreImage> findAllByStore(Store store);
}
