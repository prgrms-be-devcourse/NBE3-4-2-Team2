package com.example.backend.social.reaction.like.util.component;

import org.springframework.stereotype.Component;

import com.example.backend.entity.CommentRepository;
import com.example.backend.entity.PostRepository;
import com.example.backend.social.exception.SocialErrorCode;
import com.example.backend.social.exception.SocialException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResourceResolver {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    
    public Object resolveResource(String resourceType, Long resourceId) {
        return switch (resourceType) {
            case "post" -> postRepository.findById(resourceId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "게시물을 찾을 수 없습니다."));
            case "comment", "reply" -> commentRepository.findById(resourceId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
            default -> throw new IllegalArgumentException("리소스 타입을 확인할 수 없습니다: " + resourceType);
        };
    }
    
    public String normalizeResourceType(String resourceType) {
        return resourceType.toUpperCase();
    }
}
