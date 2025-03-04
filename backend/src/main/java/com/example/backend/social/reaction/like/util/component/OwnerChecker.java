package com.example.backend.social.reaction.like.util.component;

import org.springframework.stereotype.Component;

import com.example.backend.entity.CommentEntity;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PostEntity;

@Component
public class OwnerChecker {
	public boolean isOwner(MemberEntity member, Object resource) {
		if (resource instanceof PostEntity) {
			PostEntity post = (PostEntity) resource;
			return post.getMember().getId().equals(member.getId());
		} else if (resource instanceof CommentEntity) {
			CommentEntity comment = (CommentEntity) resource;
			return comment.getMember().getId().equals(member.getId());
		}
		throw new IllegalArgumentException("Unsupported resource type: " + resource.getClass().getName());
	}

	public Long getOwnerIdFromResource(Object resource) {
		if (resource instanceof PostEntity) {
			PostEntity post = (PostEntity) resource;
			return post.getMember().getId();
		} else if (resource instanceof CommentEntity) {
			CommentEntity comment = (CommentEntity) resource;
			return comment.getMember().getId();
		}
		throw new IllegalArgumentException("Unsupported resource type: " + resource.getClass().getName());
	}
}
