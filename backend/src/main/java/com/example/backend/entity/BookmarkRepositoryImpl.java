package com.example.backend.entity;

import static com.example.backend.entity.QBookmarkEntity.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class BookmarkRepositoryImpl implements BookmarkRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	public BookmarkRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
		Long count = queryFactory
			.select(bookmarkEntity.count())
			.from(bookmarkEntity)
			.where(bookmarkEntity.member.id.eq(memberId)
				.and(bookmarkEntity.post.id.eq(postId)))
			.fetchOne();

		return count != null && count > 0;
	}

	@Override
	public List<BookmarkEntity> findAllByMemberId(Long memberId) {
		return queryFactory
			.selectFrom(bookmarkEntity)
			.where(bookmarkEntity.member.id.eq(memberId))
			.orderBy(bookmarkEntity.createDate.desc())
			.fetch();
	}
}
