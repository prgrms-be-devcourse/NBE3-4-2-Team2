package com.example.backend.content.search.implement;

import static com.example.backend.entity.QHashtagEntity.*;
import static com.example.backend.entity.QImageEntity.*;
import static com.example.backend.entity.QMemberEntity.*;
import static com.example.backend.entity.QPostEntity.*;
import static com.example.backend.entity.QPostHashtagEntity.*;

import java.util.List;

import org.springframework.stereotype.Component;

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
		List<SearchPostResponse> searchPostResponses = jpaQueryFactory
			.select(Projections.constructor(
				SearchPostResponse.class,
				imageEntity.post.id,
				imageEntity.imageUrl))
			.from(imageEntity)
			.join(imageEntity.post, postEntity)
			.join(postEntity.member, memberEntity)
			.where(
				lastPostId != null ? imageEntity.post.id.lt(lastPostId) : null,
				memberEntity.username.eq(keyword),
				imageEntity.id.eq(
					JPAExpressions
						.select(imageEntity.id.min())
						.from(imageEntity)
						.where(imageEntity.post.id.eq(postEntity.id))))
			.orderBy(imageEntity.post.id.desc())
			.limit(size + 1)
			.fetch();

		// 딱 size 만큼 데이터가 있을 때 처리를 위해 이와 같은 방식 사용
		boolean hasNext = isHasNext(size, searchPostResponses);
		// 검색 결과가 없으면 null,
		Long newLastPostId = getNewLastPostId(searchPostResponses);

		return SearchPostCursorResponse.create(searchPostResponses, newLastPostId, hasNext);
	}

	private SearchPostCursorResponse findByHashtag(String keyword, Long lastPostId, int size) {
		List<SearchPostResponse> searchPostResponses = jpaQueryFactory
			.select(Projections.constructor(
				SearchPostResponse.class,
				imageEntity.post.id,
				imageEntity.imageUrl))
			.from(imageEntity)
			.join(imageEntity.post, postEntity)
			.join(postHashtagEntity).on(postHashtagEntity.post.eq(postEntity))
			.join(postHashtagEntity.hashtag, hashtagEntity)
			.where(
				lastPostId != null ? imageEntity.post.id.lt(lastPostId) : null,
				hashtagEntity.content.containsIgnoreCase(keyword),
				imageEntity.id.eq(
					JPAExpressions
						.select(imageEntity.id.min())
						.from(imageEntity)
						.where(imageEntity.post.id.eq(postEntity.id))))
			.orderBy(imageEntity.post.id.desc())
			.limit(size + 1)
			.fetch();

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
		if (hasNext) {
			searchPostResponses.removeLast();
		}
		return hasNext;
	}

}
