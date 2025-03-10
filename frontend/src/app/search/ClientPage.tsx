"use client";

import React, { useState, useEffect, useRef } from "react";
import { Search } from "lucide-react";
import { getImageUrl } from "@/utils/imageUtils";
import FeedDetailModal from "@/components/feed/FeedDetailModal"; // 모달 컴포넌트 import

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

// 모달에 필요한 기본 피드 데이터 타입 (FeedInfoResponse와 호환)
type BasicFeedInfo = {
  postId: number;
  authorId?: number;
  authorName?: string;
  profileImgUrl?: string;
  content?: string;
  createdDate?: string;
  imgUrlList?: string[];
  hashTagList?: string[];
  likeCount: number;
  commentCount: number;
  likeFlag: boolean;
  bookmarkId: number;
};

const ClientPage = () => {
  const [searchType, setSearchType] = useState<SearchType>("HASHTAG");
  const [keyword, setKeyword] = useState("");
  const [posts, setPosts] = useState<SearchPostResponse[]>([]);
  const [lastPostId, setLastPostId] = useState<number | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  // 모달 관련 상태 추가
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [selectedPostId, setSelectedPostId] = useState<number | null>(null);
  const [selectedFeed, setSelectedFeed] = useState<BasicFeedInfo | null>(null);

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

      // 첫 요청이거나 lastPostId가 null인 경우 0으로 설정
      if (!isInitial) {
        searchParams.lastPostId = lastPostId !== null ? lastPostId : 0;
      } else {
        // 초기 요청에도 lastPostId를 0으로 설정
        searchParams.lastPostId = 0;
      }

      // URL 매개변수로 변환
      const params = new URLSearchParams();
      Object.entries(searchParams).forEach(([key, value]) => {
        if (value !== undefined) {
          params.append(key, value.toString());
        }
      });

      // 백엔드 서버 URL을 직접 사용
      const baseUrl = "http://localhost:8080";
      const response = await fetch(`${baseUrl}/api-v1/search?${params}`, {
        // 크로스 도메인 요청에 쿠키 포함 (인증이 필요한 경우)
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(
          `검색 요청 실패: ${response.status} ${response.statusText}`
        );
      }

      const result = await response.json();

      if (result.success) {
        // 백엔드가 isSuccess 대신 success 필드를 사용하는 경우
        const data: SearchPostCursorResponse = result.data;
        setPosts((prev) =>
          isInitial
            ? data.searchPostResponses
            : [...prev, ...data.searchPostResponses]
        );
        setLastPostId(data.lastPostId);
        setHasMore(data.hasNext);
      } else {
        console.error("검색 실패:", result.message);
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

  // 이미지 클릭 시 모달 열기
  const handleImageClick = (postId: number, imageUrl: string) => {
    // 기본적인 피드 정보 설정 (모달에 표시할 최소 정보)
    const basicFeed: BasicFeedInfo = {
      postId: postId,
      authorName: "작성자", // 기본값 (실제로는 API에서 가져와야 함)
      profileImgUrl: "", // 기본값
      content: "", // 기본값
      imgUrlList: [imageUrl], // 현재 이미지
      hashTagList: [], // 기본값
      likeCount: 0, // 기본값
      commentCount: 0, // 기본값
      likeFlag: false, // 기본값
      bookmarkId: -1, // 기본값
    };

    setSelectedPostId(postId);
    setSelectedFeed(basicFeed);
    openModal();
  };

  // 모달 열기 함수
  const openModal = () => {
    setIsModalOpen(true);
    // 모달이 열릴 때 body 스크롤 방지
    document.body.style.overflow = "hidden";
  };

  // 모달 닫기 함수
  const closeModal = () => {
    setIsModalOpen(false);
    // 모달이 닫힐 때 body 스크롤 복원
    document.body.style.overflow = "";
  };

  // 모달에서 좋아요/북마크 상태가 변경되었을 때 호출될 콜백 함수
  const handleModalStateChange = (updatedFeed: any) => {
    // 상태 업데이트 로직 (필요한 경우)
    console.log("모달에서 상태 변경됨:", updatedFeed);
  };

  return (
    <div className="p-4 h-full">
      <form onSubmit={handleSearch} className="mb-8">
        <div className="flex flex-col sm:flex-row gap-4 mb-4">
          <select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value as SearchType)}
            className="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 
                      dark:bg-gray-800 dark:border-gray-700 dark:text-gray-200 dark:focus:ring-blue-400"
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
              className="w-full px-4 py-2 pl-10 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500
                        dark:bg-gray-800 dark:border-gray-700 dark:text-gray-200 dark:placeholder-gray-400 dark:focus:ring-blue-400"
            />
            <Search
              className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 dark:text-gray-500"
              size={20}
            />
          </div>

          <button
            type="submit"
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500
                      dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-400"
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
              className="aspect-square relative overflow-hidden rounded-lg border dark:border-gray-700 cursor-pointer"
              onClick={() => handleImageClick(post.postId, post.imageUrl)}
            >
              <img
                src={getImageUrl(post.imageUrl)}
                alt={`Post ${post.postId}`}
                className="w-full h-full object-cover"
              />
            </div>
          ))}
        </div>
      ) : !loading && keyword.trim() ? (
        <p className="text-center text-gray-500 dark:text-gray-400 my-8">
          검색 결과가 없습니다
        </p>
      ) : null}

      {loading && (
        <div className="flex justify-center my-8">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 dark:border-blue-400"></div>
        </div>
      )}

      {!hasMore && posts.length > 0 && (
        <p className="text-center text-gray-500 dark:text-gray-400 my-8">
          모든 게시물을 불러왔습니다
        </p>
      )}

      {/* 피드 상세 모달 */}
      {isModalOpen && selectedPostId && selectedFeed && (
        <FeedDetailModal
          feedId={selectedPostId}
          onStateChange={handleModalStateChange}
          isOpen={isModalOpen}
          onClose={closeModal}
        />
      )}
    </div>
  );
};

export default ClientPage;
