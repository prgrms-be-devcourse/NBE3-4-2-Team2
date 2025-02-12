package com.example.backend.entity;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-06
 */

@RequiredArgsConstructor
public class PostHashtagRepositoryImpl implements PostHashtagRepositoryCustom {

	private final JdbcTemplate jdbcTemplate;
	private static final String INSERT_SQL_POST_HASHTAG =
		"INSERT INTO post_hashtag (post_id, hashtag_id) VALUES (?, ?)";

	/**
	 * hashtag size 는 최대 10개이기 때문에 단순하게 처리
	 * @author kwak
	 * @since 2025-02-06
	 */
	@Override
	public void bulkInsert(PostEntity post, List<HashtagEntity> hashtags) {
		jdbcTemplate.batchUpdate(
			INSERT_SQL_POST_HASHTAG,
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int idx) throws SQLException {
					ps.setLong(1, post.getId());
					ps.setLong(2, hashtags.get(idx).getId());
				}

				@Override
				public int getBatchSize() {
					return hashtags.size();
				}
			}
		);
	}
}
