package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "image")
public class ImageEntity extends BaseEntity {

	@Column(nullable = false)
	private String imageUrl;

	@JoinColumn(nullable = false, name = "post_id")
	@ManyToOne(fetch = FetchType.LAZY)
	PostEntity post;

	public static ImageEntity create(String imageUrl, PostEntity post) {
		ImageEntity image = ImageEntity.builder()
			.imageUrl(imageUrl)
			.post(post)
			.build();
		post.addImage(image);
		return image;
	}
}
