"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { components } from "../../lib/backend/apiV1/schema";
import { useComments } from "@/components/feed/useComments"; // 커스텀 훅 import
import CommentsSection from "@/components/feed/CommentsSection"; // 댓글 컴포넌트 import
import client from "@/lib/backend/client";

type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];

export default function FeedDetail({ id }: { id: string }) {
  const router = useRouter();
  const feedId = parseInt(id);
  const [feed, setFeed] = useState<FeedInfoResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isLiked, setIsLiked] = useState<boolean>(false);
  const [isBookmarked, setIsBookmarked] = useState<boolean>(false);
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);

  // 댓글 관련 로직을 훅으로 분리
  const { comments, fetchComments, addComment, likeComment, replyToComment } =
    useComments(feedId);

  // 이미지가 있는지 확인
  const hasImages = feed?.imgUrlList && feed.imgUrlList.length > 0;

  useEffect(() => {
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
          setIsBookmarked(!!foundFeed.bookmarkId);

          // 피드를 찾은 후 댓글 데이터도 불러오기
          fetchComments();
        } else {
          console.error("피드를 찾을 수 없습니다. ID:", feedId);
        }
      } catch (error) {
        console.error(
          "피드 상세 정보를 불러오는 중 오류가 발생했습니다:",
          error
        );
      } finally {
        setLoading(false);
      }
    };

    if (feedId) {
      fetchFeedDetail();
    }
  }, [feedId, fetchComments]);

  // 뒤로가기 처리
  const handleGoBack = () => {
    // 브라우저의 뒤로가기를 사용하여 이전 페이지로 이동
    router.back();
  };

  // 좋아요 기능
  const handleLike = (): void => {
    setIsLiked(!isLiked);
    // API 호출은 여기에 구현
    // 자신의 글에는 좋아요를 할 수 없다.
    // 예: api.post(`/feeds/${feedId}/like`);
  };

  // 북마크 기능
  const handleBookmark = (): void => {
    setIsBookmarked(!isBookmarked);
    // API 호출은 여기에 구현
    // 자신의 글에도 북마크를 할 수 있다.
    // 예: api.post(`/feeds/${feedId}/bookmark`);
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

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-black">
        <div className="loading-spinner text-center">
          <div className="w-12 h-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mb-4 mx-auto"></div>
          <p className="text-white">로딩 중...</p>
        </div>
      </div>
    );
  }

  if (!feed) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-black">
        <div className="text-center text-white">
          <h2 className="text-xl font-bold mb-2">피드를 찾을 수 없습니다</h2>
          <p className="mb-4">
            요청하신 피드가 존재하지 않거나 삭제되었을 수 있습니다.
          </p>
          <button
            onClick={handleGoBack}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            피드로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black text-white">
      {/* 메인 콘텐츠 */}
      <div className="max-w-6xl mx-auto my-6 flex h-[calc(100vh-48px)]">
        {/* 피드 콘텐츠 */}
        <div className="flex-1 flex flex-col md:flex-row bg-black h-full">
          {/* 좌측: 이미지와 글 정보 */}
          <div className="md:w-[calc(100%-420px)]">
            <div className="relative">
              {/* 닫기 버튼 */}
              <button
                onClick={handleGoBack}
                className="absolute top-4 right-4 text-white bg-gray-800 bg-opacity-50 rounded-full p-1 z-10"
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

              {/* 이미지 영역 */}
              {hasImages ? (
                <div className="w-full aspect-square bg-black flex items-center justify-center relative">
                  <img
                    src={feed.imgUrlList?.[currentImageIndex]}
                    alt="피드 이미지"
                    className="max-h-full max-w-full object-contain"
                  />

                  {/* 이미지가 여러 장인 경우 네비게이션 버튼 표시 */}
                  {feed.imgUrlList && feed.imgUrlList.length > 1 && (
                    <>
                      <button
                        onClick={() => handleImageNav("prev")}
                        className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-black bg-opacity-50 rounded-full p-2"
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className="h-5 w-5"
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
                        className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-black bg-opacity-50 rounded-full p-2"
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className="h-5 w-5"
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
                      <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-1">
                        {feed.imgUrlList.map((_, idx) => (
                          <span
                            key={idx}
                            className={`block w-2 h-2 rounded-full ${
                              idx === currentImageIndex
                                ? "bg-blue-500"
                                : "bg-gray-500"
                            }`}
                          />
                        ))}
                      </div>
                    </>
                  )}
                </div>
              ) : (
                <div className="w-full aspect-square bg-gray-900 flex items-center justify-center">
                  <p className="text-gray-500">이미지가 없습니다</p>
                </div>
              )}
            </div>

            {/* 작성자 정보 및 글 내용 */}
            <div className="border-t border-gray-800 p-4">
              <div className="flex items-center mb-3">
                <div className="w-8 h-8 rounded-full bg-gray-700 flex-shrink-0"></div>
                <span className="ml-3 font-medium">{feed.authorName}</span>
              </div>

              {/* 액션 버튼 */}
              <div className="flex mb-3">
                <button
                  className={`mr-4 ${isLiked ? "text-red-500" : "text-white"}`}
                  onClick={handleLike}
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-6 w-6"
                    fill={isLiked ? "currentColor" : "none"}
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"
                    />
                  </svg>
                </button>
                <div className="flex-grow"></div>
                <button
                  className={isBookmarked ? "text-white" : "text-white"}
                  onClick={handleBookmark}
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-6 w-6"
                    fill={isBookmarked ? "currentColor" : "none"}
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"
                    />
                  </svg>
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

          {/* 우측: 댓글 영역 - 분리된 컴포넌트 사용 */}
          <CommentsSection
            comments={comments}
            onAddComment={addComment}
            onLikeComment={likeComment}
            onReplyComment={replyToComment}
          />
        </div>
      </div>
    </div>
  );
}
