package com.example.backend.entity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<FollowEntity, Long>, FollowRepositoryCustom {
}
