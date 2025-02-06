package com.example.backend.entity;

import static com.example.backend.entity.QLikesEntity.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class LikesRepositoryImpl implements LikesRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	public LikesRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public boolean existsByMemberIdAndPostId(Long memberId, Long postId) {
		Long count = queryFactory
			.select(likesEntity.count())
			.from(likesEntity)
			.where(likesEntity.member.id.eq(memberId)
				.and(likesEntity.post.id.eq(postId)))
			.fetchOne();

		return count != null && count > 0;
	}
}
