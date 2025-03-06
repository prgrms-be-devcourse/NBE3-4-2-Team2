package com.example.backend.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	private Long followingCount = 0L; // 팔로위 : 본인을 팔로우중인 인원수

	@OneToMany(mappedBy = "member")
	@Builder.Default
	private List<PostEntity> postList = new ArrayList<>();

	@OneToMany(mappedBy = "receiver") // receiver가 자기 자신 => 나를 팔로우하는
	@Builder.Default
	private List<MemberEntity> followerList = new ArrayList<>();

	@OneToMany(mappedBy = "sender") // sender가 자기 자신 => 내가 팔로잉하는
	@Builder.Default
	private List<MemberEntity> followingList = new ArrayList<>();

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

	@Transactional
	public void follow(MemberEntity receiver) {
		// 자기 자신 팔로우 불가
		if (this.equals(receiver)) {
			throw new SocialException(SocialErrorCode.CANNOT_PERFORM_ON_SELF, "자기 자신을 팔로우할 수 없습니다.");
		}

		// 이미 팔로우 중인 대상에 대해 팔로우 시도 불가
		if (this.followingList.contains(receiver)) {
			throw new SocialException(SocialErrorCode.ALREADY_EXISTS, "이미 팔로우 상태입니다.");
		}

		// 팔로우 관계 생성
		this.followingList.add(receiver);
		this.followingCount++;

		receiver.followerList.add(this);
		receiver.followerCount++;
	}

	@Transactional
	public void unfollow(MemberEntity receiver) {
		// 자기 자신 언팔로우 불가
		if (this.equals(receiver)) {
			throw new SocialException(SocialErrorCode.CANNOT_PERFORM_ON_SELF, "자기 자신을 언팔로우할 수 없습니다.");
		}

		// 팔로우 관계가 없는 대상에 대한 언팔로우 요청 불가
		if (!this.followingList.contains(receiver)) {
			throw new SocialException(SocialErrorCode.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다.");
		}

		// 팔로우 관계 제거
		this.followingList.remove(receiver);
		this.followingCount--;

		receiver.followerList.remove(this);
		receiver.followerCount--;
	}
}
