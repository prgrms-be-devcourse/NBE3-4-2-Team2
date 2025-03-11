package com.example.backend.social.reaction.like.dto;

public record LikeStateInfo(
	boolean currentlyLiked,
	boolean isNewLike
) { }
