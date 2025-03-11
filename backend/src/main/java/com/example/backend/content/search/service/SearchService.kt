package com.example.backend.content.search.service

import com.example.backend.content.search.dto.SearchPostCursorResponse
import com.example.backend.content.search.implement.SearchFinder
import com.example.backend.content.search.type.SearchType
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * @author kwak
 * 2025. 3. 11.
 */
@Service
@RequiredArgsConstructor
open class SearchService {
    private val searchFinder: SearchFinder? = null

    @Transactional(readOnly = true)
    open fun search(type: SearchType?, keyword: String?, lastPostId: Long?, size: Int): SearchPostCursorResponse {
        return searchFinder!!.findByKeyword(type, keyword, lastPostId, size)
    }
}
