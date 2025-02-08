package com.example.backend.entity;

import static com.example.backend.entity.QFollowEntity.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class FollowRepositoryImpl implements FollowRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	public FollowRepositoryImpl(JPAQueryFactory queryFactory) {
		this.queryFactory = queryFactory;
	}

	@Override
	public boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId) {
		Long count = queryFactory
			.select(followEntity.count())
			.from(followEntity)
			.where(followEntity.sender.id.eq(senderId)
				.and(followEntity.receiver.id.eq(receiverId)))
			.fetchOne();

		return count != null && count > 0;
	}

	@Override
	public int countMutualFollow(Long currentMemberId, Long memberId) {
		Long count = queryFactory
			.select(followEntity.count())
			.from(followEntity)
			.where(
				followEntity.sender.id.eq(currentMemberId)
					.and(followEntity.receiver.id.eq(memberId))
					.or(
						followEntity.sender.id.eq(memberId)
							.and(followEntity.receiver.id.eq(currentMemberId))
					)
			)
			.fetchOne();

		return count != null ? count.intValue() : 0;
	}
}

