"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import FeedItem from "./FeedItem";
import { components } from "../../lib/backend/apiV1/schema";
import client from "@/lib/backend/client";
import { syncLikeStatuses, cleanupLikeData } from "../../utils/likeUtils";

// 타입 정의
type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];

// API 요청을 위한 FeedRequest 타입 정의
interface FeedRequest {
  timestamp: string; // ISO 형식의 날짜 문자열 (LocalDateTime)
  lastPostId: number;
  maxSize: number;
}

// 스크롤 위치 저장을 위한 전역 변수 (실제로는 sessionStorage 또는 localStorage 사용 권장)
let savedScrollPosition = 0;

export default function MainFeed() {
  const [feeds, setFeeds] = useState<FeedInfoResponse[]>([]);
  const [lastTimestamp, setLastTimestamp] = useState<string | undefined>(
    undefined
  );
  const [lastPostId, setLastPostId] = useState<number | undefined>(undefined);
  const [loading, setLoading] = useState<boolean>(false);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const observer = useRef<IntersectionObserver | null>(null);
  const PAGE_SIZE = 2; // 한 번에 표시할 피드 개수
  const feedContainerRef = useRef<HTMLDivElement>(null);

  // 이미 로드된 피드 ID를 추적
  const loadedPostIds = useRef<Set<number>>(new Set());

  // 마지막 피드 요소를 관찰하는 함수
  const lastFeedElementRef = useCallback(
    (node: HTMLDivElement | null) => {
      if (loading) return;
      if (observer.current) observer.current.disconnect();

      observer.current = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && hasMore) {
          loadMoreFeeds();
        }
      });

      if (node) observer.current.observe(node);
    },
    [loading, hasMore]
  );

  // 스크롤 위치 저장 함수 (페이지 이탈 시 호출)
  const saveScrollPosition = () => {
    savedScrollPosition = window.scrollY;
    sessionStorage.setItem("scrollPosition", savedScrollPosition.toString()); // sessionStorage에 저장
    console.log(`스크롤 위치 저장: ${savedScrollPosition}px`);
  };

  // 초기 피드 로딩 및 스크롤 위치 복원, 오래된 좋아요 데이터 정리
  useEffect(() => {
    // 오래된 좋아요 데이터 정리 (1시간 이상된 데이터)
    cleanupLikeData();
    
    loadFeeds();

    // 컴포넌트가 마운트된 후 저장된 스크롤 위치로 복원
    const restoreScrollPosition = () => {
      const savedPosition = sessionStorage.getItem("scrollPosition");
      if (savedPosition) {
        const position = parseInt(savedPosition, 10);
        if (position > 0) {
          console.log(`스크롤 위치 복원: ${position}px`);
          window.scrollTo(0, position);
        }
      }
    };

    // DOM이 완전히 렌더링된 후 스크롤 위치 복원을 위해 약간의 지연 추가
    const timer = setTimeout(restoreScrollPosition, 100);

    // 페이지 이동 시 스크롤 위치 저장을 위한 이벤트 리스너 추가
    window.addEventListener("beforeunload", saveScrollPosition);

    return () => {
      clearTimeout(timer);
      window.removeEventListener("beforeunload", saveScrollPosition);
    };
  }, []);

  // 콘솔로그 디버깅용
  useEffect(() => {
    console.log(`Feed count: ${feeds.length}, hasMore: ${hasMore}`);
  }, [feeds, hasMore]);

  // API로 피드 데이터 요청하는 함수 (fetch 사용)
  const fetchFeedsFromApi = async (requestData: FeedRequest) => {
    try {
      console.log("API 요청 데이터:", requestData);

      const queryParams = new URLSearchParams();
      queryParams.append("timestamp", requestData.timestamp);
      queryParams.append("lastPostId", requestData.lastPostId.toString());
      queryParams.append("maxSize", requestData.maxSize.toString());

      console.log("메인피드 요청");
      const response = await client.GET("/api-v1/feed", {
        params: {
          query: {
            timestamp: requestData.timestamp.toString(),
            lastPostId: requestData.lastPostId.toString(),
            maxSize: requestData.maxSize.toString(),
          },
        },
      });

      if (!response.data) {
        throw new Error(`API 응답 오류: ${response.error}`);
      }

      return await response.data;
    } catch (error) {
      console.error("API 호출 중 오류 발생:", error);
      throw error;
    }
  };

  // 초기 피드 로딩 (첫 페이지)
  const loadFeeds = async () => {
    try {
      setLoading(true);

      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, "0");
      const day = String(now.getDate()).padStart(2, "0");
      const hours = String(now.getHours()).padStart(2, "0");
      const minutes = String(now.getMinutes()).padStart(2, "0");
      const seconds = String(now.getSeconds()).padStart(2, "0");

      const date = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;

      const requestData: FeedRequest = {
        timestamp: date,
        lastPostId: 0,
        maxSize: PAGE_SIZE,
      };

      const response = await fetchFeedsFromApi(requestData);

      // API 응답에서 피드 목록 추출
      let apiFeeds = response.data?.feedList || [];
      console.log("API에서 가져온 원본 피드:", apiFeeds);
      
      // 로컬 스토리지의 좋아요/북마크 상태와 동기화
      apiFeeds = syncLikeStatuses(apiFeeds);
      console.log("로컬 스토리지와 동기화된 피드:", apiFeeds);

      // 이미 로드된 피드 ID 추적
      apiFeeds.forEach((feed: FeedInfoResponse) => {
        if (feed.postId !== undefined) {
          loadedPostIds.current.add(feed.postId);
        }
      });

      setFeeds(apiFeeds);

      setHasMore(apiFeeds.length > 0);

      if (apiFeeds.length > 0) {
        const lastFeed = apiFeeds[apiFeeds.length - 1];
        setLastTimestamp(lastFeed.createdDate);
        setLastPostId(lastFeed.postId);
      } else {
        if (response.data?.lastTimestamp) {
          setLastTimestamp(response.data.lastTimestamp);
        }
        if (response.data?.lastPostId !== undefined) {
          setLastPostId(response.data.lastPostId);
        }
        setHasMore(false);
      }

      console.log("Initial load complete");
    } catch (error) {
      console.error("피드를 불러오는 중 오류가 발생했습니다:", error);
    } finally {
      setLoading(false);
    }
  };

  // 피드 상태 업데이트 함수 (FeedItem에서 상태 변경 시 호출)
  const handleFeedStateChange = (updatedFeed: FeedInfoResponse) => {
    setFeeds(prevFeeds => 
      prevFeeds.map(feed => 
        feed.postId === updatedFeed.postId ? updatedFeed : feed
      )
    );
  };

  // 추가 피드 로딩 (무한 스크롤)
  const loadMoreFeeds = async () => {
    if (loading || !hasMore) return;

    console.log("Loading more feeds");

    try {
      setLoading(true);

      if (lastTimestamp && lastPostId !== undefined) {
        const requestData: FeedRequest = {
          timestamp: lastTimestamp,
          lastPostId: lastPostId,
          maxSize: PAGE_SIZE,
        };

        const response = await fetchFeedsFromApi(requestData);

        // API 응답에서 피드 목록 추출
        let newApiFeeds = response.data?.feedList || [];
        
        // 로컬 스토리지의 좋아요/북마크 상태와 동기화
        newApiFeeds = syncLikeStatuses(newApiFeeds);

        if (newApiFeeds.length > 0) {
          // 중복 피드 필터링
          const filteredFeeds = newApiFeeds.filter(
            (feed: FeedInfoResponse) =>
              feed.postId !== undefined &&
              !loadedPostIds.current.has(feed.postId)
          );

          // 이미 로드된 피드 ID 추적
          filteredFeeds.forEach((feed: FeedInfoResponse) => {
            if (feed.postId !== undefined) {
              loadedPostIds.current.add(feed.postId);
            }
          });

          setFeeds((prevFeeds) => [...prevFeeds, ...filteredFeeds]);

          if (filteredFeeds.length > 0) {
            const lastFeed = filteredFeeds[filteredFeeds.length - 1];
            setLastTimestamp(lastFeed.createdDate);
            setLastPostId(lastFeed.postId);
          } else {
            if (response.data?.lastTimestamp) {
              setLastTimestamp(response.data.lastTimestamp);
            }
            if (response.data?.lastPostId !== undefined) {
              setLastPostId(response.data.lastPostId);
            }
          }

          setHasMore(newApiFeeds.length > 0);
        } else {
          setHasMore(false);
        }
      } else {
        console.log("No timestamp or postId available for cursor pagination");
        setHasMore(false);
      }
    } catch (error) {
      console.error("추가 피드를 불러오는 중 오류가 발생했습니다:", error);
    } finally {
      setLoading(false);
    }
  };

  // 새로고침 기능 - 최신 피드 불러오기
  const refreshFeeds = () => {
    // 로드된 피드 ID 초기화
    loadedPostIds.current.clear();
    // 피드 상태 초기화
    setFeeds([]);
    setLastTimestamp(undefined);
    setLastPostId(undefined);
    setHasMore(true);
    // 새로운 피드 로딩
    loadFeeds();
  };

  return (
    <div className="w-full max-w-[500px] mx-auto" ref={feedContainerRef}>
      {/* 새로고침 버튼 추가 (선택적) */}
      <div className="flex justify-center mb-4">
        <button 
          onClick={refreshFeeds} 
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
          disabled={loading}
        >
          {loading ? "로딩 중..." : "새로고침"}
        </button>
      </div>
      
      <div className="feed-list">
        {feeds.map((feed, index) => {
          const isLastElement = index === feeds.length - 1;

          return (
            <div
              key={`feed-${feed.postId || index}`}
              ref={isLastElement ? lastFeedElementRef : null}
            >
              <FeedItem 
                feed={feed} 
                onStateChange={handleFeedStateChange} 
              />
            </div>
          );
        })}
      </div>

      {loading && (
        <div className="loading-spinner mt-4 text-center">
          <div className="w-8 h-8 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
          <p className="text-gray-900 dark:text-gray-100 mt-2">로딩 중...</p>
        </div>
      )}

      {!hasMore && feeds.length > 0 && (
        <div className="no-more-feeds mt-4 text-center text-gray-600 dark:text-gray-400 py-4">
          더 이상 피드가 없습니다.
        </div>
      )}

      {!hasMore && feeds.length === 0 && (
        <div className="empty-feed mt-8 text-center">
          <p className="text-gray-700 dark:text-gray-300 text-lg mb-2">피드가 없습니다.</p>
          <p className="text-gray-500 dark:text-gray-400 text-sm">첫 피드를 작성해보세요!</p>
        </div>
      )}
    </div>
  );
}