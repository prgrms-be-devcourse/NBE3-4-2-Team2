package com.example.backend.social.reaction.like.util.component;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.backend.entity.MemberEntity;
import com.example.backend.global.event.LikeEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LikeEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final OwnerChecker ownerChecker;
    
    public void publishLikeEvent(MemberEntity member, Object resource, Long resourceId, String resourceType) {
        Long ownerId = ownerChecker.getOwnerIdFromResource(resource);
        applicationEventPublisher.publishEvent(
            LikeEvent.create(member.getUsername(), ownerId, resourceId, resourceType)
        );
    }
}
