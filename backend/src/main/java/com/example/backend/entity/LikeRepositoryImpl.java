package com.example.backend.entity;

import static com.example.backend.entity.QLikeEntity.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class LikeRepositoryImpl implements LikeRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	public LikeRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public Optional<LikeEntity> findByMemberIdAndResourceIdAndResourceType(long memberId, Long resourceId,
		String resourceType) {
		LikeEntity result = queryFactory
			.selectFrom(likeEntity)
			.where(
				likeEntity.member.id.eq(memberId),
				likeEntity.resourceId.eq(resourceId),
				likeEntity.resourceType.eq(resourceType)
			)
			.fetchOne();

		return Optional.ofNullable(result);
	}
}
