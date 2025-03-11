package com.example.backend.content.search.service

import com.example.backend.content.search.dto.SearchPostCursorResponse
import com.example.backend.content.search.implement.SearchFinder
import com.example.backend.content.search.type.SearchType
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author kwak
 * 2025. 3. 11.
 */
@Service
@Slf4j
open class SearchService(
    private val searchFinder: SearchFinder // 생성자 주입 방식
) {
    @Transactional(readOnly = true)
    open fun search(type: SearchType?, keyword: String?, lastPostId: Long?, size: Int): SearchPostCursorResponse {
        val result = searchFinder.findByKeyword(type, keyword, lastPostId, size)
        log.info("Searching for keyword: $keyword, result: $result")
        return result
    }
}
