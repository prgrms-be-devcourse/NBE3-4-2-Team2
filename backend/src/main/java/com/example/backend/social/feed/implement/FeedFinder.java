package com.example.backend.social.feed.implement;

import static com.example.backend.entity.QCommentEntity.*;
import static com.example.backend.entity.QFollowEntity.*;
import static com.example.backend.entity.QHashtagEntity.*;
import static com.example.backend.entity.QImageEntity.*;
import static com.example.backend.entity.QLikesEntity.*;
import static com.example.backend.entity.QPostEntity.*;
import static com.example.backend.entity.QPostHashtagEntity.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.backend.entity.MemberEntity;
import com.example.backend.social.feed.Feed;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/***
 * FeedFinder
 * 피드 객체를 제공하는 컴포넌트
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
@Component
@RequiredArgsConstructor
public class FeedFinder {

	private JPAQueryFactory queryFactory;

	/***
	 * 팔로워가 팔로우중인 Member들의 게시물을 얻는다.
	 * 파라미터로 넘어오는 timestamp 이전에 등록된 게시물들에 한해서 최대 limit 개수만큼 리스트에 담는다.
	 * @param member 팔로워 Entity 객체
	 * @param timestamp 최근 받아간 피드의 시간
	 * @param limit 한 번에 받아올 리스트의 최대 크기
	 * @return 피드 리스트
	 */
	public List<Feed> findByFollower(final MemberEntity member, final LocalDateTime timestamp, final int limit) {
		return queryFactory
			.select(Projections.constructor(Feed.class,
				postEntity,
				GroupBy.list(hashtagEntity.content),
				GroupBy.list(imageEntity.imageUrl),
				likesEntity.count().intValue(),
				commentEntity.count().intValue()
			))
			.from(postEntity)
			.join(postEntity.member).fetchJoin()                          // N+1 문제 해결을 위한 fetch join
			.join(followEntity).on(followEntity.sender.eq(member)         // 현재 로그인한 사용자가 sender
				.and(followEntity.receiver.eq(postEntity.member)))        // 게시글 작성자가 receiver
			.leftJoin(postHashtagEntity).on(postHashtagEntity.post.eq(postEntity))
			.leftJoin(hashtagEntity).on(postHashtagEntity.hashtag.eq(hashtagEntity))
			.leftJoin(imageEntity).on(imageEntity.post.eq(postEntity))
			.leftJoin(likesEntity).on(likesEntity.post.eq(postEntity))
			.leftJoin(commentEntity).on(commentEntity.post.eq(postEntity))
			.where(postEntity.createDate.before(timestamp))
			.groupBy(postEntity.id)
			.orderBy(postEntity.createDate.desc())
			.limit(limit)
			.fetch();
	}

	public List<Feed> findRecommendFinder(final LocalDateTime timestamp, final int limit) {
		return List.of();
	}
}
