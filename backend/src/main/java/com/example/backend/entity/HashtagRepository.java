package com.example.backend.entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<HashtagEntity, Long> {

	Optional<HashtagEntity> findByContent(String content);

}
