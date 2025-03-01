package com.example.backend.entity;

import java.util.Optional;

public interface LikeRepositoryCustom {
	Optional<LikeEntity> findByMemberIdAndResourceIdAndResourceType(long memberId, Long resourceId, String resourceType);
}
