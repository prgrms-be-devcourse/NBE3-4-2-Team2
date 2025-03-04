"use client";

import React, { useState, useEffect, useRef } from "react";
import { Search } from "lucide-react";

type SearchPostResponse = {
  postId: number;
  imageUrl: string;
};

type SearchPostCursorResponse = {
  searchPostResponses: SearchPostResponse[];
  lastPostId: number | null;
  hasNext: boolean;
};

// 백엔드의 Enum과 일치하도록 정의
type SearchType = "AUTHOR" | "HASHTAG";

// 검색 API 요청을 위한 인터페이스
interface SearchParams {
  type: SearchType;
  keyword: string;
  lastPostId?: number;
  size?: number;
}

const ClientPage = () => {
  const [searchType, setSearchType] = useState<SearchType>("HASHTAG");
  const [keyword, setKeyword] = useState("");
  const [posts, setPosts] = useState<SearchPostResponse[]>([]);
  const [lastPostId, setLastPostId] = useState<number | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  const observerRef = useRef<IntersectionObserver | null>(null);
  const lastPostRef = useRef<HTMLDivElement | null>(null);

  const fetchPosts = async (isInitial = false) => {
    if (loading || (!hasMore && !isInitial) || !keyword.trim()) return;

    try {
      setLoading(true);

      // 검색 매개변수를 명시적인 타입으로 정의
      const searchParams: SearchParams = {
        type: searchType,
        keyword: keyword,
        size: 12,
      };

      if (!isInitial && lastPostId) {
        searchParams.lastPostId = lastPostId;
      }

      // URL 매개변수로 변환
      const params = new URLSearchParams();
      Object.entries(searchParams).forEach(([key, value]) => {
        if (value !== undefined) {
          params.append(key, value.toString());
        }
      });

      const response = await fetch(`/api-v1/search?${params}`);
      const result = await response.json();

      if (result.isSuccess) {
        const data: SearchPostCursorResponse = result.data;
        setPosts((prev) =>
          isInitial
            ? data.searchPostResponses
            : [...prev, ...data.searchPostResponses]
        );
        setLastPostId(data.lastPostId);
        setHasMore(data.hasNext);
      }
    } catch (error) {
      console.error("Failed to fetch posts:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (
          entries[0].isIntersecting &&
          hasMore &&
          !loading &&
          keyword.trim()
        ) {
          fetchPosts();
        }
      },
      { threshold: 0.5 }
    );

    observerRef.current = observer;

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [hasMore, loading, keyword]);

  useEffect(() => {
    if (lastPostRef.current && observerRef.current) {
      observerRef.current.observe(lastPostRef.current);
    }

    return () => {
      if (observerRef.current && lastPostRef.current) {
        observerRef.current.unobserve(lastPostRef.current);
      }
    };
  }, [posts]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!keyword.trim()) return;

    setPosts([]);
    setLastPostId(null);
    setHasMore(true);
    fetchPosts(true);
  };

  return (
    <div className="p-4">
      <form onSubmit={handleSearch} className="mb-8">
        <div className="flex flex-col sm:flex-row gap-4 mb-4">
          <select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value as SearchType)}
            className="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="HASHTAG">해시태그</option>
            <option value="AUTHOR">작성자</option>
          </select>

          <div className="flex-1 relative">
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder={
                searchType === "HASHTAG"
                  ? "#태그를 입력하세요"
                  : "작성자를 입력하세요"
              }
              className="w-full px-4 py-2 pl-10 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <Search
              className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
              size={20}
            />
          </div>

          <button
            type="submit"
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={!keyword.trim() || loading}
          >
            검색
          </button>
        </div>
      </form>

      {posts.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {posts.map((post, index) => (
            <div
              key={post.postId}
              ref={index === posts.length - 1 ? lastPostRef : null}
              className="aspect-square relative overflow-hidden rounded-lg"
            >
              <img
                src={post.imageUrl}
                alt={`Post ${post.postId}`}
                className="w-full h-full object-cover"
              />
            </div>
          ))}
        </div>
      ) : !loading && keyword.trim() ? (
        <p className="text-center text-gray-500 my-8">검색 결과가 없습니다</p>
      ) : null}

      {loading && (
        <div className="flex justify-center my-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
        </div>
      )}

      {!hasMore && posts.length > 0 && (
        <p className="text-center text-gray-500 my-8">
          모든 게시물을 불러왔습니다
        </p>
      )}
    </div>
  );
};

export default ClientPage;
