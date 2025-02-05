package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
	Optional<PostEntity> findByIdAndIsDeletedFalse(Long postid);
}
