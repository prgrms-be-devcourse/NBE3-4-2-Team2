package com.example.backend.content.search.implement;

import static com.example.backend.entity.QHashtagEntity.*;
import static com.example.backend.entity.QImageEntity.*;
import static com.example.backend.entity.QMemberEntity.*;
import static com.example.backend.entity.QPostEntity.*;
import static com.example.backend.entity.QPostHashtagEntity.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.backend.content.search.dto.ImageDataWithPostId;
import com.example.backend.content.search.dto.SearchPostCursorResponse;
import com.example.backend.content.search.dto.SearchPostResponse;
import com.example.backend.content.search.exception.SearchErrorCode;
import com.example.backend.content.search.exception.SearchException;
import com.example.backend.content.search.type.SearchType;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * type 에 따라 분기로 처리되는 동적쿼리
 * 게시글의 첫번째 이미지만 받아서 반환
 * @author kwak
 * 2025-02-06
 */
@Component
@RequiredArgsConstructor
public class SearchFinder {

	private final JPAQueryFactory jpaQueryFactory;

	public SearchPostCursorResponse findByKeyword(SearchType type, String keyword, Long lastPostId, int size) {
		if (type == SearchType.AUTHOR) {
			return findByAuthor(keyword, lastPostId, size);
		} else if (type == SearchType.HASHTAG) {
			return findByHashtag(keyword, lastPostId, size);
		}
		throw new SearchException(SearchErrorCode.INVALID_SEARCH_TYPE);
	}

	private SearchPostCursorResponse findByAuthor(String keyword, Long lastPostId, int size) {
		// 1. postId 먼저 추출
		List<Long> postIds = jpaQueryFactory
			.select(postEntity.id)
			.from(postEntity)
			.join(postEntity.member, memberEntity)
			.where(
				lastPostId != 0L ? postEntity.id.lt(lastPostId) : null,
				memberEntity.username.eq(keyword))
			.orderBy(postEntity.id.desc())
			.limit(size + 1)
			.fetch();

		// 2. 이미지가 없는 경우의 처리를 위해 postIds 개수 만큼 SearchPostResponse 미리 초기화
		// imgUrl 은 모두 null 인 상태
		Map<Long, SearchPostResponse> searchPostInitMap = new LinkedHashMap<>();
		for (Long postId : postIds) {
			searchPostInitMap.put(postId, SearchPostResponse.create(postId, null));
		}

		// 3. allImages 의 최대 크기는 (postIds * image 최대 개수)
		// 성능 최적화) 1차 검색 결과에서 일치하는 postIds 가 없으면 실행하지 않음
		if (!postIds.isEmpty()) {

			List<ImageDataWithPostId> allImages = jpaQueryFactory
				.select(Projections.constructor(
					ImageDataWithPostId.class,
					imageEntity.post.id,
					imageEntity.imageUrl
				))
				.from(imageEntity)
				.where(
					imageEntity.post.id.in(postIds)
					// 서브쿼리 삭제
					// imageEntity.id.eq(
					// 	JPAExpressions
					// 		.select(imageEntity.id.min())
					// 		.from(imageEntity)
					// 		.where(imageEntity.post.id.eq(postEntity.id)))
				)
				.orderBy(imageEntity.post.id.desc(), imageEntity.id.asc()) // 이미지는 id 순서대로 저장된다고 가정
				.limit(size + 1)
				.fetch();

			// 첫번째 이미지만을 searchPostInitMap 에 담기 위해 다음과 같이 처리
			for (ImageDataWithPostId imageData : allImages) {
				if (searchPostInitMap.get(imageData.postId()).imageUrl() == null) {
					searchPostInitMap.put(imageData.postId(),
						SearchPostResponse.create(imageData.postId(), imageData.imgUrl()));
				}
			}
		}

		List<SearchPostResponse> searchPostResponses = searchPostInitMap.values().stream().toList();

		// 딱 size 만큼 데이터가 있을 때 처리를 위해 이와 같은 방식 사용
		boolean hasNext = isHasNext(size, searchPostResponses);
		// 검색 결과가 없으면 null,
		Long newLastPostId = getNewLastPostId(searchPostResponses);

		return SearchPostCursorResponse.create(searchPostResponses, newLastPostId, hasNext);
	}

	private SearchPostCursorResponse findByHashtag(String keyword, Long lastPostId, int size) {

		List<Long> postIds = jpaQueryFactory
			.select(postEntity.id)
			.from(postEntity)
			.join(postHashtagEntity).on(postHashtagEntity.post.eq(postEntity))
			.join(postHashtagEntity.hashtag, hashtagEntity)
			.where(
				lastPostId != 0L ? postEntity.id.lt(lastPostId) : null,
				hashtagEntity.content.eq(keyword)
			)
			.orderBy(postEntity.id.desc())
			.limit(size + 1)
			.fetch();

		Map<Long, SearchPostResponse> searchPostInitMap = new LinkedHashMap<>();
		for (Long postId : postIds) {
			searchPostInitMap.put(postId, SearchPostResponse.create(postId, null));
		}

		if (!postIds.isEmpty()) {

			List<ImageDataWithPostId> allImages = jpaQueryFactory
				.select(Projections.constructor(
					ImageDataWithPostId.class,
					imageEntity.post.id,
					imageEntity.imageUrl))
				.from(imageEntity)
				.where(imageEntity.post.id.in(postIds))
				.orderBy(imageEntity.post.id.desc(), imageEntity.id.asc())
				.limit(size + 1)
				.fetch();

			for (ImageDataWithPostId imageData : allImages) {
				if (searchPostInitMap.get(imageData.postId()).imageUrl() == null) {
					searchPostInitMap.put(imageData.postId(),
						SearchPostResponse.create(imageData.postId(), imageData.imgUrl()));
				}
			}
		}
		List<SearchPostResponse> searchPostResponses = searchPostInitMap.values().stream().toList();

		// 딱 size 만큼 데이터가 있을 때 처리를 위해 이와 같은 방식 사용
		boolean hasNext = isHasNext(size, searchPostResponses);
		// 검색 결과가 없으면 null,
		Long newLastPostId = getNewLastPostId(searchPostResponses);

		return SearchPostCursorResponse.create(searchPostResponses, newLastPostId, hasNext);
	}

	private Long getNewLastPostId(List<SearchPostResponse> searchPostResponses) {
		return searchPostResponses.isEmpty() ? null : searchPostResponses.getLast().postId();
	}

	private boolean isHasNext(int size, List<SearchPostResponse> searchPostResponses) {
		boolean hasNext = searchPostResponses.size() > size;
		// + 1 만큼 추가 조회한거 제거
		if (hasNext) {
			searchPostResponses.removeLast();
		}
		return hasNext;
	}

}
