package com.example.backend.content.hashtag.service

import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * #으로 시작되는 해시태그를 추출할 수 있는 클래스
 * @author kwak
 * 2025. 3. 11.
 */
@Component
class HashtagExtractor {
    fun extractHashtag(content: String): Set<String> {
        val hashtags: MutableSet<String> = LinkedHashSet()
        val matcher = HASHTAG_EXTRACT_PATTERN.matcher(content)

        while (matcher.find()) {
            hashtags.add(matcher.group(1))
        }

        return hashtags
    }

    companion object {
        private val HASHTAG_EXTRACT_PATTERN: Pattern = Pattern.compile("#([\\w가-힣]+)")
    }
}

