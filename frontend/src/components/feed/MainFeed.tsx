"use client";

import { useState, useEffect, useRef, useCallback } from "react";
import FeedItem from "./FeedItem";
import { dummyFeeds } from "./dummyData";
import { components } from "../../lib/backend/apiV1/schema";

// 타입 정의
type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];

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
  const PAGE_SIZE = 5; // 한 번에 표시할 피드 개수
  const [page, setPage] = useState<number>(0);
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
    console.log(
      `Feed count: ${feeds.length}, hasMore: ${hasMore}, page: ${page}`
    );
  }, [feeds, hasMore, page]);

  // 더미 데이터로 초기 피드 로딩
  const loadFeeds = () => {
    try {
      setLoading(true);

      // 더미 데이터 사용 - 처음 5개 항목
      const initialFeeds = dummyFeeds.feedList?.slice(0, PAGE_SIZE) || [];

      // ID 추적 세트에 추가
      initialFeeds.forEach((feed) => {
        if (feed.postId !== undefined) {
          loadedPostIds.current.add(feed.postId);
        }
      });

      setFeeds(initialFeeds);

      // 마지막 피드의 타임스탬프와 ID 설정
      if (initialFeeds.length > 0) {
        const lastFeed = initialFeeds[initialFeeds.length - 1];
        setLastTimestamp(lastFeed.createdDate);
        setLastPostId(lastFeed.postId);
      }

      setHasMore((dummyFeeds.feedList?.length || 0) > initialFeeds.length);
      setPage(1);

      console.log("Initial load complete");
    } catch (error) {
      console.error("피드를 불러오는 중 오류가 발생했습니다:", error);
    } finally {
      setLoading(false);
    }
  };

  // 더미 데이터로 추가 피드 로딩 (무한 스크롤)
  const loadMoreFeeds = () => {
    if (loading || !hasMore) return;

    console.log("Loading more feeds");

    try {
      setLoading(true);

      // 더미 데이터 사용 - 다음 페이지 항목들
      const start = page * PAGE_SIZE;
      const end = start + PAGE_SIZE;

      console.log(`Slicing from index ${start} to ${end}`);

      const nextFeeds = dummyFeeds.feedList?.slice(start, end) || [];

      if (nextFeeds.length > 0) {
        // 중복 데이터 필터링
        const newFeeds = nextFeeds.filter(
          (feed) =>
            feed.postId !== undefined && !loadedPostIds.current.has(feed.postId)
        );

        // 새 피드가 없으면 더 이상 불러올 데이터가 없음을 표시
        if (newFeeds.length === 0) {
          console.log("No new feeds to load, ending infinite scroll");
          setHasMore(false);
          return;
        }

        // ID 추적 세트에 추가
        newFeeds.forEach((feed) => {
          if (feed.postId !== undefined) {
            loadedPostIds.current.add(feed.postId);
          }
        });

        setFeeds((prevFeeds) => [...prevFeeds, ...newFeeds]);

        // 마지막 피드의 타임스탬프와 ID 설정
        const lastFeed = newFeeds[newFeeds.length - 1];
        setLastTimestamp(lastFeed.createdDate);
        setLastPostId(lastFeed.postId);

        setPage((prevPage) => prevPage + 1);
        setHasMore(end < (dummyFeeds.feedList?.length || 0));

        console.log(`Added ${newFeeds.length} new feeds`);
      } else {
        console.log("No more feeds available");
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
      <h2 className="text-xl font-bold mb-4">SNS 피드</h2>

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

      {!hasMore && feeds.length > 0 && (
        <div className="no-more-feeds mt-4 text-center text-gray-500">
          더 이상 피드가 없습니다.
        </div>
      )}
    </div>
  );
}
