package com.example.backend.social.feed.implement;

import static com.example.backend.entity.QCommentEntity.*;
import static com.example.backend.entity.QFollowEntity.*;
import static com.example.backend.entity.QHashtagEntity.*;
import static com.example.backend.entity.QImageEntity.*;
import static com.example.backend.entity.QLikesEntity.*;
import static com.example.backend.entity.QPostEntity.*;
import static com.example.backend.entity.QPostHashtagEntity.*;
import static com.example.backend.social.feed.constant.FeedConstants.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.backend.entity.MemberEntity;
import com.example.backend.social.feed.Feed;
import com.example.backend.social.feed.schedular.FeedScheduler;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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
	private final FeedScheduler scheduler;

	/***
	 * 팔로워가 팔로우중인 Member 들의 게시물을 얻는다.
	 * 파라미터로 넘어오는 timestamp 이전에 등록된 게시물들에 한해서 최대 limit 개수만큼 리스트에 담는다.
	 * timestamp 가 동일한 경우에 대비하여 lastPostId를 같이 받아서 처리한다.
	 * @param member 팔로워 Entity 객체
	 * @param timestamp 최근 받아간 피드의 시간
	 * @param lastPostId 최근 받아간 피드 중 가장 마지막 ID
	 * @param limit 한 번에 받아올 리스트의 최대 크기
	 * @return 피드 리스트
	 */
	public List<Feed> findByFollower(final MemberEntity member, final LocalDateTime timestamp,
		final Long lastPostId, final int limit) {

		// Post 정보와 count 를 조회
		List<Feed> feeds = queryFactory.select(Projections.constructor(Feed.class, postEntity,
				JPAExpressions.select(likesEntity.count()).from(likesEntity).where(likesEntity.post.eq(postEntity)),
				JPAExpressions.select(commentEntity.count()).from(commentEntity).where(commentEntity.post.eq(postEntity))))
			.from(postEntity)
			.join(postEntity.member)
			.fetchJoin()
			.leftJoin(followEntity)
			.on(followEntity.sender.eq(member).and(followEntity.receiver.eq(postEntity.member)))
			.where(
				cursor(timestamp, lastPostId)
					.and(
						followEntity.id.isNotNull()
							.or(postEntity.member.eq(member))
					))
			.groupBy(postEntity)
			.orderBy(postEntity.createDate.desc())
			.limit(limit)
			.fetch();

		fillFeedData(feeds);
		return feeds;
	}

	/**
	 * 추천 게시물을 취합하여 반환한다
	 * 팔로잉 게시물과 member 자신의 게시물은 제외한다
	 * @param member 요청한 유저의 멤버 Entity 객체
	 * @param timestamp 가장 최근 받은 게시물의 timestamp
	 * @param lastTime 추천 게시물을 요청할 범위
	 * @param limit 추천 게시물 요청 페이징 개수
	 * @return 피드 리스트
	 */
	public List<Feed> findRecommendFinder(final MemberEntity member, final LocalDateTime timestamp,
		final LocalDateTime lastTime, final int limit) {

		// 이거로 구할 수 있는 것 => 좋아요 개수가 많은 순, 댓글 수가 많은 순으로 구할 수 있다.
		List<Feed> feeds = queryFactory.select(Projections.constructor(Feed.class, postEntity,
				JPAExpressions.select(likesEntity.count()).from(likesEntity).where(likesEntity.post.eq(postEntity)),
				JPAExpressions.select(commentEntity.count()).from(commentEntity).where(commentEntity.post.eq(postEntity))))
			.from(postEntity)
			.join(postEntity.member)
			.fetchJoin()
			.where((postEntity.createDate.before(timestamp).and(postEntity.createDate.after(lastTime))
				.or(postEntity.createDate.before(timestamp).and(postEntity.createDate.eq(lastTime))))
				.and(postEntity.member.id.notIn(
					JPAExpressions.select(followEntity.receiver.id)
						.from(followEntity)
						.where(followEntity.sender.eq(member))))
				.and(postEntity.member.id.notIn(member.getId())))
			.orderBy(
				// 좋아요 개수 / 팔로워 수 / 댓글 수에 각각 점수를 매겨서 정렬
				Expressions.numberTemplate(Double.class,
						"(select count(*) * 3 from LikesEntity l where l.post.id = {0}) + "
							+ "(select count(*) * 2 from FollowEntity f where f.receiver.id = {1}) + "
							+ "(select count(*) from CommentEntity c where c.post.id = {0}) + "
							+ "(select case when count(*) > 0 then 3 else 0 end "
							+ "from PostHashtagEntity ph where ph.post.id = {0} and ph.hashtag.id in ({2}))",
						postEntity.id,
						postEntity.member.id,
						scheduler.getPopularHashtagList())
					.desc())
			.limit(limit * RECOMMEND_RANDOM_POOL_MULTIPLIER)
			.fetch();

		// 랜덤하게 뽑는다
		Collections.shuffle(feeds);
		feeds = feeds.subList(0, Math.min(limit, feeds.size()));

		fillFeedData(feeds);
		return feeds;
	}

	private void fillFeedData(List<Feed> feeds) {

		List<Long> postIds = feeds.stream().map(feed -> feed.getPost().getId()).collect(Collectors.toList());

		Map<Long, List<String>> hashtagsByPostId = queryFactory.select(postHashtagEntity.post.id, hashtagEntity.content)
			.from(postHashtagEntity)
			.join(hashtagEntity)
			.on(postHashtagEntity.hashtag.eq(hashtagEntity))
			.where(postHashtagEntity.post.id.in(postIds))
			.fetch()  // Tuple 리스트로 조회
			.stream()
			.collect(Collectors.groupingBy(tuple -> tuple.get(0, Long.class),        // postId로 그룹핑
				Collectors.mapping(tuple -> tuple.get(1, String.class),            // content를 리스트로 수집
					Collectors.toList())));

		Map<Long, List<String>> imageUrlsByPostId = queryFactory.select(imageEntity.post.id, imageEntity.imageUrl)
			.from(imageEntity)
			.where(imageEntity.post.id.in(postIds))
			.fetch()  // Tuple 리스트로 조회
			.stream()
			.collect(Collectors.groupingBy(tuple -> tuple.get(0, Long.class),    // postId로 그룹핑
				Collectors.mapping(tuple -> tuple.get(1, String.class),            // imageUrl을 리스트로 수집
					Collectors.toList())));

		feeds.forEach(feed -> {
			Long postId = feed.getPost().getId();
			feed.setHashTagList(hashtagsByPostId.getOrDefault(postId, new ArrayList<>()));
			feed.setImageUrlList(imageUrlsByPostId.getOrDefault(postId, new ArrayList<>()));
		});

	}

	private BooleanExpression cursor(LocalDateTime timestamp, Long lastPostId) {
		if (lastPostId == null) {
			return postEntity.createDate.before(timestamp);
		}

		return postEntity.createDate.loe(timestamp)
			.and(postEntity.id.lt(lastPostId));
	}
}
