package com.example.backend.content.search.service;

import org.springframework.stereotype.Service;

import com.example.backend.content.search.dto.SearchPostCursorResponse;
import com.example.backend.content.search.implement.SearchFinder;
import com.example.backend.content.search.type.SearchType;

import lombok.RequiredArgsConstructor;

/**
 * @author kwak
 * 2025-02-06
 */
@Service
@RequiredArgsConstructor
public class SearchService {

	private final SearchFinder searchFinder;

	public SearchPostCursorResponse search(SearchType type, String keyword, Long lastPostId, int size) {
		return searchFinder.findByKeyword(type, keyword, lastPostId, size);
	}
}
