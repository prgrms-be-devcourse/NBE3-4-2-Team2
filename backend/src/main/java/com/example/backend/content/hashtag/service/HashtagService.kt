package com.example.backend.content.hashtag.service

import com.example.backend.content.hashtag.exception.HashtagErrorCode
import com.example.backend.content.hashtag.exception.HashtagException
import com.example.backend.entity.HashtagEntity
import com.example.backend.entity.HashtagRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * @author kwak
 * 2025. 3. 11.
 */
@Service
class HashtagService(
    private val hashtagRepository: HashtagRepository,
    private val collector: HashtagUsageCollector
) {
    fun createIfNotExists(content: String?): HashtagEntity {
        val hashtag = hashtagRepository.findByContent(content)
            .orElseGet {
                hashtagRepository.save(
                    HashtagEntity.create(content)
                )
            }

        collector.addUsageStorage(hashtag.id)

        return hashtag
    }

    fun findByContent(content: String?): HashtagEntity {
        return hashtagRepository.findByContent(content)
            .orElseThrow { HashtagException(HashtagErrorCode.NOT_FOUND) }
    }

    fun deleteOldHashtag(oldHashtagIds: List<Long?>?) {
        hashtagRepository.bulkDeleteByIds(oldHashtagIds)
    }

    fun findOldHashtags(): List<Long> {
        return hashtagRepository.findOldHashtags(
            LocalDateTime.now()
                .minusMonths(OLD_HASHTAG_MONTH)
        )
    }

    fun bulkLastUsedAt(hashtagUsageData: Set<Long?>?, now: LocalDateTime?) {
        hashtagRepository.bulkLastUsedAt(hashtagUsageData, now)
    }

    companion object {
        private const val OLD_HASHTAG_MONTH: Long = 3
    }
}
