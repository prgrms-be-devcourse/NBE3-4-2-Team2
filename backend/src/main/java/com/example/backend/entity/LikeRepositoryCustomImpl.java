package com.example.backend.entity;

import static com.example.backend.entity.QLikeEntity.likeEntity;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import com.example.backend.social.reaction.like.dto.LikeInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryCustomImpl implements LikeRepositoryCustom {
	private final JPAQueryFactory queryFactory;
	private final EntityManager entityManager;
	private final JdbcTemplate jdbcTemplate;

	@Override
	public Optional<LikeEntity> findByMemberIdAndResourceIdAndResourceType(
		long memberId,
		Long resourceId,
		String resourceType
	) {
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

	@Override
	public int bulkUpsertLikes(List<LikeInfo> likeInfos) {
		if (likeInfos.isEmpty()) {
			return 0;
		}

		// 일괄 처리를 위한 그룹화
		Map<String, List<LikeInfo>> likesByType = likeInfos.stream()
			.collect(Collectors.groupingBy(LikeInfo::resourceType));

		int totalUpdated = 0;

		// 각 리소스 타입별로 처리
		for (Map.Entry<String, List<LikeInfo>> entry : likesByType.entrySet()) {
			String resourceType = entry.getKey();
			List<LikeInfo> likes = entry.getValue();

			// 새로운 좋아요 (삽입 필요)
			List<LikeInfo> newLikes = likes.stream()
				.filter(like -> like.createDate() != null)
				.collect(Collectors.toList());

			// 기존 좋아요 (업데이트 필요)
			List<LikeInfo> existingLikes = likes.stream()
				.filter(like -> like.createDate() == null)
				.collect(Collectors.toList());

			// 새 좋아요 일괄 삽입
			if (!newLikes.isEmpty()) {
				totalUpdated += bulkInsertLikes(newLikes, resourceType);
			}

			// 기존 좋아요 일괄 업데이트
			if (!existingLikes.isEmpty()) {
				totalUpdated += bulkUpdateLikes(existingLikes, resourceType);
			}
		}

		return totalUpdated;
	}

	private int bulkInsertLikes(List<LikeInfo> newLikes, String resourceType) {
		// Bulk insert 구현
		String sql = "INSERT INTO likes (member_id, resource_id, resource_type, create_date, updated_date, is_liked) " +
			"VALUES (?, ?, ?, ?, ?, ?)";

		int[] result = jdbcTemplate.batchUpdate(
			sql,
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					LikeInfo like = newLikes.get(i);
					ps.setLong(1, like.memberId());
					ps.setLong(2, like.resourceId());
					ps.setString(3, like.resourceType());
					ps.setTimestamp(4, Timestamp.valueOf(like.createDate()));
					ps.setTimestamp(5, Timestamp.valueOf(like.modifyDate()));
					ps.setBoolean(6, like.isActive());
				}

				@Override
				public int getBatchSize() {
					return newLikes.size();
				}
			}
		);

		return result.length;
	}

	private int bulkUpdateLikes(List<LikeInfo> existingLikes, String resourceType) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE likes SET is_liked = CASE ");

		List<Object> params = new ArrayList<>();

		for (int i = 0; i < existingLikes.size(); i++) {
			LikeInfo likeInfo = existingLikes.get(i);
			sb.append("WHEN (member_id = ? AND resource_id = ? AND resource_type = ?) THEN ? ");
			params.add(likeInfo.memberId());
			params.add(likeInfo.resourceId());
			params.add(likeInfo.resourceType());
			params.add(likeInfo.isActive());
		}

		sb.append("ELSE is_liked END, ");
		sb.append("updated_date = CASE ");

		for (int i = 0; i < existingLikes.size(); i++) {
			LikeInfo likeInfo = existingLikes.get(i);
			sb.append("WHEN (member_id = ? AND resource_id = ? AND resource_type = ?) THEN ? ");
			params.add(likeInfo.memberId());
			params.add(likeInfo.resourceId());
			params.add(likeInfo.resourceType());
			params.add(Timestamp.valueOf(likeInfo.modifyDate()));
		}

		sb.append("ELSE updated_date END ");
		sb.append("WHERE (");

		for (int i = 0; i < existingLikes.size(); i++) {
			LikeInfo likeInfo = existingLikes.get(i);
			if (i > 0) sb.append(" OR ");
			sb.append("(member_id = ? AND resource_id = ? AND resource_type = ?)");
			params.add(likeInfo.memberId());
			params.add(likeInfo.resourceId());
			params.add(likeInfo.resourceType());
		}

		sb.append(")");

		// Native query 실행
		Query query = entityManager.createNativeQuery(sb.toString());
		for (int i = 0; i < params.size(); i++) {
			query.setParameter(i + 1, params.get(i));
		}

		return query.executeUpdate();
	}
}
