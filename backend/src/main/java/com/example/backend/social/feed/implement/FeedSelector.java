package com.example.backend.social.feed.implement;

import static com.example.backend.entity.QCommentEntity.*;
import static com.example.backend.entity.QFollowEntity.*;
import static com.example.backend.entity.QHashtagEntity.*;
import static com.example.backend.entity.QImageEntity.*;
import static com.example.backend.entity.QLikesEntity.*;
import static com.example.backend.entity.QPostEntity.*;
import static com.example.backend.entity.QPostHashtagEntity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.backend.entity.MemberEntity;
import com.example.backend.social.feed.Feed;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/***
 * FeedSelector
 * 피드 객체를 제공하는 컴포넌트
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Component
@RequiredArgsConstructor
public class FeedSelector {

	private final JPAQueryFactory queryFactory;

	/***
	 * 팔로워가 팔로우중인 Member들의 게시물을 얻는다.
	 * 파라미터로 넘어오는 timestamp 이전에 등록된 게시물들에 한해서 최대 limit 개수만큼 리스트에 담는다.
	 * @param member 팔로워 Entity 객체
	 * @param timestamp 최근 받아간 피드의 시간
	 * @param limit 한 번에 받아올 리스트의 최대 크기
	 * @return 피드 리스트
	 */
	public List<Feed> findByFollower(final MemberEntity member, final LocalDateTime timestamp, final int limit) {

		// 1. 먼저 기본 Post 정보와 count들을 조회
		List<Feed> feeds = queryFactory
			.select(Projections.constructor(Feed.class,
				postEntity,
				JPAExpressions
					.select(likesEntity.count())
					.from(likesEntity)
					.where(likesEntity.post.eq(postEntity)),
				JPAExpressions
					.select(commentEntity.count())
					.from(commentEntity)
					.where(commentEntity.post.eq(postEntity))
			))
			.from(postEntity)
			.join(postEntity.member).fetchJoin()
			.join(followEntity)
			.on(followEntity.sender.eq(member)
				.and(followEntity.receiver.eq(postEntity.member)))
			.where(postEntity.createDate.before(timestamp))
			.orderBy(postEntity.createDate.desc())
			.limit(limit)
			.fetch();

		// 2. 조회된 Post들의 ID 리스트
		List<Long> postIds = feeds.stream()
			.map(feed -> feed.getPost().getId())
			.collect(Collectors.toList());

		// 3. 해시태그 정보 조회
		Map<Long, List<String>> hashtagsByPostId = queryFactory
			.select(postHashtagEntity.post.id, hashtagEntity.content)
			.from(postHashtagEntity)
			.join(hashtagEntity).on(postHashtagEntity.hashtag.eq(hashtagEntity))
			.where(postHashtagEntity.post.id.in(postIds))
			.fetch()  // Tuple 리스트로 조회
			.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(0, Long.class),  // postId로 그룹핑
				Collectors.mapping(
					tuple -> tuple.get(1, String.class),  // content를 리스트로 수집
					Collectors.toList()
				)
			));

		// 4. 이미지 URL 정보 조회
		Map<Long, List<String>> imageUrlsByPostId = queryFactory
			.select(imageEntity.post.id, imageEntity.imageUrl)
			.from(imageEntity)
			.where(imageEntity.post.id.in(postIds))
			.fetch()  // Tuple 리스트로 조회
			.stream()
			.collect(Collectors.groupingBy(
				tuple -> tuple.get(0, Long.class),  // postId로 그룹핑
				Collectors.mapping(
					tuple -> tuple.get(1, String.class),  // imageUrl을 리스트로 수집
					Collectors.toList()
				)
			));

		// 5. Feed 객체에 해시태그와 이미지 URL 정보를 설정
		feeds.forEach(feed -> {
			Long postId = feed.getPost().getId();
			feed.setHashTagList(hashtagsByPostId.getOrDefault(postId, new ArrayList<>()));
			feed.setImageUrlList(imageUrlsByPostId.getOrDefault(postId, new ArrayList<>()));
		});

		return feeds;
	}

	public List<Feed> findRecommendFinder(final LocalDateTime timestamp, final int limit) {
		return List.of();
	}
}
