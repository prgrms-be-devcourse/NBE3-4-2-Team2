package com.example.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<LikeEntity, Long>, LikeRepositoryCustom {
}
