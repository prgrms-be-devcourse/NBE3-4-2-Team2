"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import FeedItem from "./FeedItem";
import { components } from "../../lib/backend/apiV1/schema";
import client from "@/lib/backend/client";

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
    console.log(`스크롤 위치 저장: ${savedScrollPosition}px`);
  };

  // 초기 피드 로딩 및 스크롤 위치 복원
  useEffect(() => {
    loadFeeds();

    // 컴포넌트가 마운트된 후 저장된 스크롤 위치로 복원
    const restoreScrollPosition = () => {
      if (savedScrollPosition > 0) {
        console.log(`스크롤 위치 복원: ${savedScrollPosition}px`);
        window.scrollTo(0, savedScrollPosition);
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

      // 쿼리 파라미터 생성
      const queryParams = new URLSearchParams();
      queryParams.append("timestamp", requestData.timestamp);
      queryParams.append("lastPostId", requestData.lastPostId.toString());
      queryParams.append("maxSize", requestData.maxSize.toString());

      // API 호출 - GET 요청으로 쿼리 파라미터 전달 (백엔드 서버 주소 사용)
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

      // 응답 데이터 반환
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

      // 처음 요청하는 경우: timestamp에는 현재 시간, lastPostId에는 0을 넣음
      // Java LocalDateTime 형식에 맞는 날짜 문자열 생성 (yyyy-MM-dd'T'HH:mm:ss)
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, "0");
      const day = String(now.getDate()).padStart(2, "0");
      const hours = String(now.getHours()).padStart(2, "0");
      const minutes = String(now.getMinutes()).padStart(2, "0");
      const seconds = String(now.getSeconds()).padStart(2, "0");

      // LocalDateTime 형식에 맞는 포맷: "yyyy-MM-dd'T'HH:mm:ss"
      const date = `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;

      const requestData: FeedRequest = {
        timestamp: date,
        lastPostId: 0,
        maxSize: PAGE_SIZE,
      };

      const response = await fetchFeedsFromApi(requestData);

      // 응답 데이터 처리 - 수정된 응답 구조에 맞춤
      const apiFeeds = response.data?.feedList || [];
      console.log(apiFeeds);

      // ID 추적 세트에 추가
      apiFeeds.forEach((feed: FeedInfoResponse) => {
        if (feed.postId !== undefined) {
          loadedPostIds.current.add(feed.postId);
        }
      });

      setFeeds(apiFeeds);

      // 더 피드가 있는지 확인 (feedList 길이로 판단)
      setHasMore(apiFeeds.length > 0);

      // 마지막 피드의 타임스탬프와 ID 설정
      if (apiFeeds.length > 0) {
        const lastFeed = apiFeeds[apiFeeds.length - 1];
        setLastTimestamp(lastFeed.createdDate);
        setLastPostId(lastFeed.postId);
      } else {
        // 응답에서 받은 lastTimestamp와 lastPostId 사용
        if (response.data?.lastTimestamp) {
          setLastTimestamp(response.data.lastTimestamp);
        }
        if (response.data?.lastPostId !== undefined) {
          setLastPostId(response.data.lastPostId);
        }
        // 피드가 없는 경우 더 불러올 내용 없음
        setHasMore(false);
      }

      console.log("Initial load complete");
    } catch (error) {
      console.error("피드를 불러오는 중 오류가 발생했습니다:", error);
    } finally {
      setLoading(false);
    }
  };

  // 추가 피드 로딩 (무한 스크롤)
  const loadMoreFeeds = async () => {
    if (loading || !hasMore) return;

    console.log("Loading more feeds");

    try {
      setLoading(true);

      if (lastTimestamp && lastPostId !== undefined) {
        // 이후 요청부터는 Response로 전달받은 값을 다시 전달하여 다음 게시물을 받음
        const requestData: FeedRequest = {
          timestamp: lastTimestamp,
          lastPostId: lastPostId,
          maxSize: PAGE_SIZE,
        };

        const response = await fetchFeedsFromApi(requestData);

        // 응답 데이터 처리 - 수정된 응답 구조에 맞춤
        const newApiFeeds = response.data?.feedList || [];

        if (newApiFeeds.length > 0) {
          // 중복 데이터 필터링
          const filteredFeeds = newApiFeeds.filter(
            (feed: FeedInfoResponse) =>
              feed.postId !== undefined &&
              !loadedPostIds.current.has(feed.postId)
          );

          // ID 추적 세트에 추가
          filteredFeeds.forEach((feed: FeedInfoResponse) => {
            if (feed.postId !== undefined) {
              loadedPostIds.current.add(feed.postId);
            }
          });

          setFeeds((prevFeeds) => [...prevFeeds, ...filteredFeeds]);

          // 마지막 피드의 타임스탬프와 ID 설정
          if (filteredFeeds.length > 0) {
            const lastFeed = filteredFeeds[filteredFeeds.length - 1];
            setLastTimestamp(lastFeed.createdDate);
            setLastPostId(lastFeed.postId);
          } else {
            // 응답에서 받은 lastTimestamp와 lastPostId 사용
            if (response.data?.lastTimestamp) {
              setLastTimestamp(response.data.lastTimestamp);
            }
            if (response.data?.lastPostId !== undefined) {
              setLastPostId(response.data.lastPostId);
            }
          }

          // 응답에서 받은 피드 리스트가 비어있지 않으면 더 불러올 데이터가 있다고 판단
          setHasMore(newApiFeeds.length > 0);
        } else {
          // 피드가 없는 경우 더 불러올 내용 없음
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

  return (
    <div className="w-full max-w-[500px] mx-auto" ref={feedContainerRef}>
      <div className="feed-list">
        {feeds.map((feed, index) => {
          // 마지막 요소에만 ref 설정
          const isLastElement = index === feeds.length - 1;

          return (
            <div
              key={`feed-${feed.postId || index}`}
              ref={isLastElement ? lastFeedElementRef : null}
            >
              <FeedItem feed={feed} />
            </div>
          );
        })}
      </div>

      {loading && (
        <div className="loading-spinner mt-4 text-center">
          <div className="spinner"></div>
          <p>로딩 중...</p>
        </div>
      )}

      {!hasMore && (
        <div className="no-more-feeds mt-4 text-center text-gray-500">
          더 이상 피드가 없습니다.
        </div>
      )}
    </div>
  );
}
