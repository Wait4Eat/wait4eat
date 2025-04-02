package com.example.wait4eat.domain.store_image.repository;

import com.example.wait4eat.domain.store_image.entity.StoreImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreImageRepository extends JpaRepository<StoreImage, Long> {
}
