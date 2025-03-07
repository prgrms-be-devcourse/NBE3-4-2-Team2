package com.example.backend.social.feed

import com.example.backend.entity.PostEntity

/**
 * Feed
 * Feed Domain 내부의 비즈니스 로직에서 사용될 개념적 객체
 * @author ChoiHyunSan
 * @since 2025-01-31
 */
data class Feed(
    val post: PostEntity,
    var commentCount: Long,
    var hashTagList: List<String> = emptyList(),
    var imageUrlList: List<String> = emptyList(),
    var bookmarkId: Long? = null,
    var isLiked: Boolean = false
) {
    // 보조 생성자
    constructor(post: PostEntity, commentCount: Long) : this(
        post = post,
        commentCount = commentCount,
        hashTagList = emptyList(),
        imageUrlList = emptyList(),
        bookmarkId = null,
        isLiked = false
    )

    /**
     * 피드 데이터를 채우는 함수
     */
    fun fillData(
        hashTagList: List<String>,
        imageUrlList: List<String>,
        bookmarkId: Long?,
        isLiked: Boolean
    ) {
        this.hashTagList = hashTagList
        this.imageUrlList = imageUrlList
        this.bookmarkId = bookmarkId
        this.isLiked = isLiked
    }
}
