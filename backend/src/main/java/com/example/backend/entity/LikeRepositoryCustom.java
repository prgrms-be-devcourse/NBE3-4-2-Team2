package com.example.backend.entity;

import java.util.List;
import java.util.Optional;

import com.example.backend.social.reaction.like.dto.LikeInfo;

public interface LikeRepositoryCustom {
	Optional<LikeEntity> findByMemberIdAndResourceIdAndResourceType(long memberId, Long resourceId, String resourceType);
	int bulkUpsertLikes(List<LikeInfo> likeInfos);
}
