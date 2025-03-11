package com.example.backend.content.hashtag.service

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 사용된 hashtag 의 데이터를 수집하고 처리
 * @author kwak
 * 2025. 3. 11.
 */
@Component
class HashtagUsageCollector {
    private val usageStorage: MutableSet<Long> = ConcurrentHashMap.newKeySet()

    fun addUsageStorage(hashtagId: Long) {
        usageStorage.add(hashtagId)
    }

    fun flushUsageStorage(): Set<Long> {
        val copy: Set<Long> = HashSet(usageStorage)
        usageStorage.clear()
        return copy
    }
}
