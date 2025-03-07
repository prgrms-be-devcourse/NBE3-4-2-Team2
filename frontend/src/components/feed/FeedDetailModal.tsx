"use client";

import { useState, useEffect } from "react";
import { components } from "../../lib/backend/apiV1/schema";
import { useComments } from "@/components/feed/useComments";
import CommentsSection from "@/components/feed/CommentsSection";
import client from "@/lib/backend/client";
import { getImageUrl } from "@/utils/imageUtils";

type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];

interface FeedDetailModalProps {
  feedId: number;
  feed?: FeedInfoResponse; // 피드 데이터를 props로 받음
  initialLikeState?: boolean; // 초기 좋아요 상태
  initialBookmarkState?: boolean; // 초기 북마크 상태
  onStateChange?: (updatedFeed: FeedInfoResponse) => void; // 상태 변경 콜백
  isOpen: boolean;
  onClose: () => void;
}

export default function FeedDetailModal({
  feedId,
  feed: initialFeed,
  initialLikeState,
  initialBookmarkState,
  onStateChange,
  isOpen,
  onClose,
}: FeedDetailModalProps) {
  const [feed, setFeed] = useState<FeedInfoResponse | null>(
    initialFeed || null
  );
  const [loading, setLoading] = useState<boolean>(!initialFeed);
  const [isLiked, setIsLiked] = useState<boolean>(
    initialLikeState !== undefined ? initialLikeState : false
  );
  const [isBookmarked, setIsBookmarked] = useState<boolean>(
    initialBookmarkState !== undefined ? initialBookmarkState : false
  );
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);

  // 댓글 관련 로직을 훅으로 분리
  const { comments, fetchComments, addComment, likeComment, replyToComment } =
    useComments(feedId);

  // 이미지가 있는지 확인
  const hasImages = feed?.imgUrlList && feed.imgUrlList.length > 0;

  useEffect(() => {
    // 모달이 열렸을 때만 데이터 로드 (initialFeed가 없는 경우에만)
    if (isOpen && feedId && !initialFeed) {
      fetchFeedDetail();
    } else if (initialFeed && isOpen) {
      // initialFeed가 있으면 댓글만 불러옴
      fetchComments();
    }
  }, [feedId, isOpen, initialFeed]);

  // 단일 피드 정보 불러오기
  const fetchFeedDetail = async () => {
    setLoading(true);

    try {
      console.log(`피드 ID: ${feedId} 데이터 불러오는 중...`);
      const response = await client.GET("/api-v1/feed/{postId}", {
        params: {
          path: {
            postId: feedId,
          },
        },
      });

      if (!response.data) {
        throw new Error(`API 응답 오류: ${response.error}`);
      }

      const foundFeed = response.data.data;
      if (foundFeed) {
        console.log("피드를 찾았습니다:", foundFeed);
        setFeed(foundFeed);

        // 초기 상태가 전달되지 않은 경우에만 API 데이터로 설정
        if (initialLikeState === undefined) {
          setIsLiked(!!foundFeed.likeFlag);
        }
        if (initialBookmarkState === undefined) {
          setIsBookmarked(foundFeed.bookmarkId != -1);
        }

        // 피드를 찾은 후 댓글 데이터도 불러오기
        fetchComments();
      } else {
        console.error("피드를 찾을 수 없습니다. ID:", feedId);
      }
    } catch (error) {
      console.error("피드 상세 정보를 불러오는 중 오류가 발생했습니다:", error);
    } finally {
      setLoading(false);
    }
  };

  // 좋아요 기능
  const handleLike = async (e: React.MouseEvent): Promise<void> => {
    e.stopPropagation();
    if (!feed) return;

    console.log(
      isLiked
        ? isLiked + "좋아요를 취소합니다."
        : isLiked + "좋아요를 누릅니다."
    );

    // API 호출
    try {
      const response = await client.POST("/api-v1/like/{id}", {
        params: {
          path: {
            id: feed.postId,
          },
          query: {
            resourceType: "post",
          },
        },
      });

      if (response.response.status == 200) {
        const newIsLiked = !isLiked;
        setIsLiked(newIsLiked);

        // 좋아요 수 업데이트
        const newLikeCount = newIsLiked
          ? (feed.likeCount || 0) + 1
          : (feed.likeCount || 0) - 1;

        // feed 객체 업데이트
        const updatedFeed = {
          ...feed,
          likeFlag: newIsLiked,
          likeCount: newLikeCount,
        };

        setFeed(updatedFeed);

        // 부모 컴포넌트에 상태 변경 알림
        if (onStateChange) {
          onStateChange(updatedFeed);
        }
      }
    } catch (error) {
      console.error("좋아요 처리 중 오류:", error);
    }
  };

  // 북마크 기능
  const handleBookmark = async (e: React.MouseEvent): Promise<void> => {
    e.stopPropagation();
    if (!feed) return;

    console.log(
      isBookmarked
        ? isBookmarked + "북마크를 취소합니다."
        : isBookmarked + "북마크를 추가합니다."
    );

    // API 호출
    try {
      const response = isBookmarked
        ? await client.DELETE("/api-v1/bookmark/{postId}", {
            params: {
              path: {
                postId: feed.postId,
              },
            },
            body: {
              bookmarkId: feed.bookmarkId,
            },
          })
        : await client.POST("/api-v1/bookmark/{postId}", {
            params: {
              path: {
                postId: feed.postId,
              },
            },
          });

      const newIsBookmarked = !isBookmarked;
      setIsBookmarked(newIsBookmarked);

      // feed 객체 업데이트
      const updatedFeed = {
        ...feed,
        bookmarkId: newIsBookmarked
          ? response.data?.data?.bookmarkId || feed.bookmarkId
          : -1,
      };

      setFeed(updatedFeed);

      // 부모 컴포넌트에 상태 변경 알림
      if (onStateChange) {
        onStateChange(updatedFeed);
      }
    } catch (error) {
      console.error("북마크 처리 중 오류:", error);
    }
  };

  // 이미지 다음/이전 이동 기능
  const handleImageNav = (direction: "next" | "prev") => {
    if (!feed?.imgUrlList || feed.imgUrlList.length <= 1) return;

    if (direction === "next") {
      setCurrentImageIndex((prev) =>
        prev === feed.imgUrlList!.length - 1 ? 0 : prev + 1
      );
    } else {
      setCurrentImageIndex((prev) =>
        prev === 0 ? feed.imgUrlList!.length - 1 : prev - 1
      );
    }
  };

  // 모달이 닫혀 있으면 아무것도 렌더링하지 않음
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-gray-500 bg-opacity-75 flex items-center justify-center">
      <div className="relative max-w-6xl w-full max-h-[90vh] flex flex-col bg-white dark:bg-gray-800 rounded-lg shadow-xl">
        {/* 닫기 버튼 */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-700 dark:text-gray-300 bg-gray-200 dark:bg-gray-700 bg-opacity-80 rounded-full p-1 z-10 hover:bg-gray-300 dark:hover:bg-gray-600"
          aria-label="닫기"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
              clipRule="evenodd"
            />
          </svg>
        </button>

        {loading ? (
          <div className="flex-1 flex items-center justify-center">
            <div className="loading-spinner text-center">
              <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mb-4 mx-auto"></div>
              <p className="text-gray-700 dark:text-gray-300">로딩 중...</p>
            </div>
          </div>
        ) : !feed ? (
          <div className="flex-1 flex items-center justify-center text-center text-gray-800 dark:text-gray-200">
            <div>
              <h2 className="text-xl font-bold mb-2">
                피드를 찾을 수 없습니다
              </h2>
              <p className="mb-4">
                요청하신 피드가 존재하지 않거나 삭제되었을 수 있습니다.
              </p>
              <button
                onClick={onClose}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                닫기
              </button>
            </div>
          </div>
        ) : (
          <div className="flex-1 flex flex-col md:flex-row text-gray-800 dark:text-gray-200 overflow-hidden">
            {/* 좌측: 이미지와 글 정보 */}
            <div className="md:w-[55%] flex flex-col overflow-hidden">
              {/* 이미지 영역 - 크기 제한 추가 */}
              <div className="flex-1 flex items-center justify-center bg-gray-100 dark:bg-gray-900 relative h-full max-h-[calc(60vh)]">
                {hasImages ? (
                  <div className="w-full h-full relative overflow-hidden flex items-center justify-center">
                    <img
                      src={getImageUrl(feed.imgUrlList?.[currentImageIndex])}
                      alt="피드 이미지"
                      className="max-h-full max-w-full object-contain"
                      style={{
                        width: "auto",
                        height: "auto",
                        maxHeight: "100%",
                        maxWidth: "100%",
                      }}
                    />

                    {/* 이미지가 여러 장인 경우 네비게이션 버튼 표시 */}
                    {feed.imgUrlList && feed.imgUrlList.length > 1 && (
                      <>
                        <button
                          onClick={() => handleImageNav("prev")}
                          className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-white dark:bg-gray-700 bg-opacity-70 dark:bg-opacity-70 rounded-full p-2 z-10 hover:bg-opacity-100"
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5 text-gray-800 dark:text-gray-200"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </button>
                        <button
                          onClick={() => handleImageNav("next")}
                          className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-white dark:bg-gray-700 bg-opacity-70 dark:bg-opacity-70 rounded-full p-2 z-10 hover:bg-opacity-100"
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className="h-5 w-5 text-gray-800 dark:text-gray-200"
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </button>

                        {/* 이미지 인디케이터 */}
                        <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-1 z-10">
                          {feed.imgUrlList.map((_, idx) => (
                            <span
                              key={idx}
                              className={`block w-2 h-2 rounded-full ${
                                idx === currentImageIndex
                                  ? "bg-blue-500"
                                  : "bg-gray-500 dark:bg-gray-400"
                              }`}
                            />
                          ))}
                        </div>
                      </>
                    )}
                  </div>
                ) : (
                  <div className="w-full h-full flex items-center justify-center">
                    <p className="text-gray-400">이미지가 없습니다</p>
                  </div>
                )}
              </div>

              {/* 작성자 정보 및 글 내용 */}
              <div className="border-t border-gray-200 dark:border-gray-700 p-4 overflow-y-auto min-h-[25vh]">
                <div className="flex items-center mb-3">
                  <div className="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex-shrink-0 overflow-hidden">
                    {feed.profileImgUrl && (
                      <img
                        src={getImageUrl(feed.profileImgUrl)}
                        alt="프로필"
                        className="w-full h-full object-cover"
                      />
                    )}
                  </div>
                  <span className="ml-3 font-medium">{feed.authorName}</span>
                </div>

                {/* 액션 버튼 */}
                <div className="flex mb-3">
                  <button
                    className={`mr-4 ${
                      isLiked ? "text-red-500" : "text-gray-700 dark:text-gray-300"
                    }`}
                    onClick={handleLike}
                  >
                    <span className="text-xl mr-1">
                      {isLiked ? "❤️" : "🤍"}
                    </span>
                  </button>
                  <div className="flex-grow"></div>
                  <button
                    className={isBookmarked ? "text-blue-500" : "text-gray-700 dark:text-gray-300"}
                    onClick={handleBookmark}
                  >
                    <span className="text-xl">
                      {!isBookmarked ? "🔖" : "🏷️"}
                    </span>
                  </button>
                </div>

                {/* 좋아요 수 */}
                <div className="my-2">
                  <span className="font-medium text-sm">
                    {feed.likeCount || 0} 좋아요
                  </span>
                </div>

                {/* 글 내용 */}
                <div className="my-3">
                  <p className="text-sm">
                    <span className="font-medium mr-2">{feed.authorName}</span>
                    {feed.content}
                  </p>
                </div>

                {/* 해시태그 */}
                {feed.hashTagList && feed.hashTagList.length > 0 && (
                  <div className="my-2">
                    {feed.hashTagList.map((tag, idx) => (
                      <span key={idx} className="text-blue-400 text-sm mr-2">
                        #{tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* 우측: 댓글 영역 */}
            <div className="md:w-[45%] border-l border-gray-200 dark:border-gray-700 overflow-y-auto">
              <CommentsSection
                comments={comments}
                onAddComment={addComment}
                onLikeComment={likeComment}
                onReplyComment={replyToComment}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
