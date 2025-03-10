package com.example.backend.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
@Table(name = "member")
public class MemberEntity extends BaseEntity {

	@Column(unique = true, nullable = false)
	private String username;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	private String profileUrl;

	private String phoneNumber;

	@Column(nullable = false)
	@Builder.Default
	private Long followerCount = 0L; // 팔로워 : 본인이 팔로우중인 인원수

	@Column(nullable = false)
	@Builder.Default
	private Long followingCount = 0L; // 팔로잉 : 본인을 팔로우중인 인원수

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<PostEntity> postList = new ArrayList<>();

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "member_following_usernames", joinColumns = @JoinColumn(name = "member_id"))
	@Column(name = "following_username")
	@Builder.Default
	private List<String> followingList = new ArrayList<>(); // 내가 팔로우하는 유저 List

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "member_follower_usernames", joinColumns = @JoinColumn(name = "member_id"))
	@Column(name = "follower_username")
	@Builder.Default
	private List<String> followerList = new ArrayList<>(); // 나를 팔로우하는 유저 List

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<LikeEntity> likeList = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<CommentEntity> commentList = new ArrayList<>();

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<BookmarkEntity> bookmarkList = new ArrayList<>();

	public MemberEntity(long id, String username) {
		super();
		setId(id);
		this.username = username;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getAuthoritiesAsStringList()
			.stream()
			.map(SimpleGrantedAuthority::new)
			.toList();
	}

	public List<String> getAuthoritiesAsStringList() {
		List<String> authorities = new ArrayList<>();

		if (username.equals("admin")) // 우선 간단하게 설정
			authorities.add("ROLE_ADMIN");

		return authorities;
	}

	public void addFollowing(MemberEntity receiver) {
		this.followingList.add(receiver.getUsername());
		this.followingCount++;
	}

	public void addFollower(MemberEntity sender) {
		this.followerList.add(sender.getUsername());
		this.followerCount++;
	}

	public void removeFollowing(MemberEntity receiver) {
		if(this.followingList.remove(receiver.getUsername())) {
			this.followingCount--;
		}
	}

	public void removeFollower(MemberEntity sender) {
		if(this.followerList.remove(sender.getUsername())) {
			this.followerCount--;
		}
	}

	// Kotlin을 위한 Getter 메서드
	public List<String> getFollowingList() {
		return this.followingList;
	}

	public List<String> getFollowerList() {
		return this.followerList;
	}

	public String getUsername() {
		return this.username;
	}
}
