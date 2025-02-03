package com.example.backend.entity;

import static jakarta.persistence.GenerationType.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {
	@Id
	@GeneratedValue(strategy = IDENTITY) // AUTO_INCREMENT
	@Setter(AccessLevel.PROTECTED)
	@EqualsAndHashCode.Include
	private Long id;

	@CreatedDate
	@Setter(AccessLevel.PRIVATE)
	private LocalDateTime createDate;

	@LastModifiedDate
	@Setter(AccessLevel.PRIVATE)
	private LocalDateTime modifyDate;
}
