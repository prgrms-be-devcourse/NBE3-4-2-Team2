package com.example.backend.content.hashtag.service

import com.example.backend.entity.PostEntity
import com.example.backend.entity.PostHashtagEntity
import com.example.backend.entity.PostHashtagRepository
import org.apache.commons.collections4.SetUtils
import org.springframework.stereotype.Service
import java.util.stream.Collectors

/**
 * @author kwak
 * 2025. 3. 11.
 */
@Service
class PostHashtagService(
    private val hashtagService: HashtagService,
    private val postHashtagRepository: PostHashtagRepository
) {
    fun create(post: PostEntity?, contents: Set<String?>) {
        val hashtags = contents.stream()
            .map { content: String? -> hashtagService.createIfNotExists(content) }
            .toList()

        postHashtagRepository.bulkInsert(post, hashtags)
    }

    fun deleteByHashtagIds(oldHashtagIds: List<Long?>?) {
        postHashtagRepository.bulkDeleteByHashtagIds(oldHashtagIds)
    }

    private fun findPostHashtagByPostId(postId: Long?): List<PostHashtagEntity> {
        return postHashtagRepository.findPostHashtagByPostId(postId)
    }

    fun updatePostHashtags(post: PostEntity, newHashtags: Set<String?>) {
        val postHashtags = findPostHashtagByPostId(post.id)

        // 기존 post 의 hashtags 추출
        val currentHashtags = postHashtags.stream().map { ph: PostHashtagEntity -> ph.hashtag.content }.collect(
            Collectors.toSet()
        )

        val deletedHashtagContents: Set<String?> = SetUtils.difference(currentHashtags, newHashtags)
        val updatedHashtags: Set<String?> = SetUtils.difference(newHashtags, currentHashtags)

        // 없어진 해시태그들이 있으면 연결관계를 삭제
        if (deletedHashtagContents.isNotEmpty()) {
            postHashtagRepository.deleteByPostIdAndHashtagContent(post.id, deletedHashtagContents)
        }

        // 새로운 해시태그들이 존재하면 저장
        if (updatedHashtags.isNotEmpty()) {
            create(post, updatedHashtags)
        }
    }
}
