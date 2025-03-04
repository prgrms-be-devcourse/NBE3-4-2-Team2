package com.example.backend.entity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<ImageEntity, Long> {
	List<ImageEntity> findAllByPostId(Long postId);
}
