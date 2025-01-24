package com.example.backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
@Table(name = "member")
public class MemberEntity extends BaseEntity {

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<PostEntity> postList = new ArrayList<>();

	@OneToMany(mappedBy = "receiver") // receiver가 자기 자신 => 나를 팔로우하는
	@Builder.Default
	private List<FollowEntity> followerList = new ArrayList<>();

	@OneToMany(mappedBy = "sender") // sender가 자기 자신 => 내가 팔로잉하는
	@Builder.Default
	private List<FollowEntity> followingList = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<NotificationEntity> notificationList = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<LikeEntity> likeList = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<CommentEntity> commentList = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<BookmarkEntity> bookmarkList = new ArrayList<>();
}
