package com.example.backend.entity;

import java.util.List;

/**
 * @author kwak
 * 2025-02-06
 */
public interface PostHashtagRepositoryCustom {

	void bulkInsert(PostEntity post, List<HashtagEntity> hashtag);
}
