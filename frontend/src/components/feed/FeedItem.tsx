/* eslint-disable @next/next/no-img-element */
"use client";

import { useState, useRef } from "react";
import { components } from "../../lib/backend/apiV1/schema";
import { getImageUrl } from "../../utils/imageUtils";
import client from "@/lib/backend/client";
import FeedDetailModal from "@/components/feed/FeedDetailModal"; // 모달 컴포넌트 import
type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];

interface FeedItemProps {
  feed: FeedInfoResponse;
  isActive?: boolean;
}

// 간단한 날짜 포맷팅 함수
const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")} ${String(
    date.getHours()
  ).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
};

const FeedItem: React.FC<FeedItemProps> = ({ feed, isActive = false }) => {
  const [isLiked, setIsLiked] = useState<boolean>(!!feed.likeFlag);
  const [likeCount, setLikeCount] = useState<number>(feed.likeCount || 0);
  const [isBookmarked, setIsBookmarked] = useState<boolean>(
    !!(feed.bookmarkId != -1)
  );
  const [showAllContent, setShowAllContent] = useState<boolean>(false);
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);
  // 모달 상태 추가
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  // 슬라이더 참조
  const sliderRef = useRef<HTMLDivElement>(null);

  // 좋아요 기능
  const handleLike = async (e: React.MouseEvent): Promise<void> => {
    e.stopPropagation(); // 이벤트 전파 중지
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
        setLikeCount((prevCount) =>
          newIsLiked ? prevCount + 1 : prevCount - 1
        );
        // feed 객체의 좋아요 상태도 업데이트
        feed.likeFlag = newIsLiked;
        feed.likeCount = newIsLiked ? likeCount + 1 : likeCount - 1;
      }
    } catch (error) {
      console.error("좋아요 처리 중 오류:", error);
    }
  };

  // 북마크 기능
  const handleBookmark = async (e: React.MouseEvent): Promise<void> => {
    e.stopPropagation(); // 이벤트 전파 중지

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

      if (!isBookmarked && response.data?.data?.bookmarkId) {
        feed.bookmarkId = response.data.data.bookmarkId;
        console.log("북마크 아이디 추가. " + feed.bookmarkId);
      } else if (isBookmarked) {
        feed.bookmarkId = -1; // 북마크 취소 시 bookmarkId 초기화
      }
    } catch (error) {
      console.error("북마크 처리 중 오류:", error);
    }
  };

  // 댓글 버튼 클릭 시 모달 열기
  const handleCommentClick = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    openModal();
  };

  // 피드 아이템 클릭 이벤트 - 모달을 열지 않고 일반적인 피드 아이템 동작만 수행
  const handleFeedClick = (): void => {
    // 피드 아이템 클릭 시 추가 동작이 필요하면 여기에 구현
    console.log("피드 아이템 클릭됨");
  };

  // 모달에서 좋아요/북마크 상태가 변경되었을 때 호출될 콜백 함수
  const handleModalStateChange = (updatedFeed: FeedInfoResponse): void => {
    // 피드 정보 업데이트
    setIsLiked(!!updatedFeed.likeFlag);
    setLikeCount(updatedFeed.likeCount || 0);
    setIsBookmarked(updatedFeed.bookmarkId != -1);

    // feed 객체도 업데이트
    feed.likeFlag = updatedFeed.likeFlag;
    feed.likeCount = updatedFeed.likeCount;
    feed.bookmarkId = updatedFeed.bookmarkId;
  };

  // 모달 열기 함수
  const openModal = (): void => {
    setIsModalOpen(true);
    // 모달이 열릴 때 body 스크롤 방지
    document.body.style.overflow = "hidden";
  };

  // 모달 닫기 함수
  const closeModal = (): void => {
    setIsModalOpen(false);
    // 모달이 닫힐 때 body 스크롤 복원
    document.body.style.overflow = "";
  };

  const handleProfileImage = (e: React.MouseEvent): void => {
    e.stopPropagation();
    console.log("프로필로 이동합니다." + feed.authorId);
  };

  // 이미지 슬라이더 이동
  const goToImage = (index: number, e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    if (index < 0) {
      setCurrentImageIndex(0);
    } else if (index >= (feed.imgUrlList?.length || 0)) {
      setCurrentImageIndex((feed.imgUrlList?.length || 1) - 1);
    } else {
      setCurrentImageIndex(index);
    }
  };

  // 다음 이미지로 이동
  const nextImage = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    goToImage(currentImageIndex + 1, e);
  };

  // 이전 이미지로 이동
  const prevImage = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    goToImage(currentImageIndex - 1, e);
  };

  // 내용 더 보기/접기 버튼
  const toggleContent = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    setShowAllContent(!showAllContent);
  };

  // 날짜 포맷팅
  const formattedDate = feed.createdDate ? formatDate(feed.createdDate) : "";

  // 내용이 긴 경우 더보기 기능
  const maxLength = 100;
  const isContentLong = feed.content && feed.content.length > maxLength;
  const displayContent = showAllContent
    ? feed.content
    : isContentLong
    ? `${feed.content?.substring(0, maxLength)}...`
    : feed.content;

  // 이미지가 있는지 확인
  const hasImages = feed.imgUrlList && feed.imgUrlList.length > 0;
  // 여러 이미지가 있는지 확인
  const hasMultipleImages = feed.imgUrlList && feed.imgUrlList.length > 1;

  return (
    <>
      <div
        className={`bg-white dark:bg-gray-800 rounded-lg shadow-sm mb-4 overflow-hidden ${
          isActive ? "border-blue-400 border-2" : ""
        }`}
        onClick={handleFeedClick}
      >
        {/* 작성자 정보 */}
        <div className="flex items-center justify-between p-3">
          <div className="flex items-center" onClick={handleProfileImage}>
            <div className="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex-shrink-0 overflow-hidden">
              {feed.profileImgUrl && (
                <img
                  src={getImageUrl(feed.profileImgUrl)}
                  alt={feed.authorName}
                  className="w-full h-full object-cover"
                />
              )}
            </div>
            <span className="ml-2 font-medium text-sm text-black dark:text-white">
              {feed.authorName}
            </span>
          </div>
        </div>

        {/* 이미지 슬라이더 */}
        {hasImages && (
          <div className="relative overflow-hidden">
            <div
              className="flex transition-transform duration-300 ease-in-out"
              style={{ transform: `translateX(-${currentImageIndex * 100}%)` }}
              ref={sliderRef}
            >
              {feed.imgUrlList?.map((imgUrl, idx) => (
                <div key={idx} className="w-full flex-shrink-0">
                  <img
                    src={getImageUrl(imgUrl)}
                    alt={`피드 이미지 ${idx + 1}`}
                    className="w-full h-auto object-cover aspect-square"
                  />
                </div>
              ))}
            </div>

            {/* 좌우 화살표 버튼 */}
            {hasMultipleImages && (
              <>
                <button
                  className={`absolute left-2 top-1/2 transform -translate-y-1/2 bg-white dark:bg-gray-700 bg-opacity-70 dark:bg-opacity-70 rounded-full w-8 h-8 flex items-center justify-center ${
                    currentImageIndex === 0
                      ? "opacity-50 cursor-not-allowed"
                      : "opacity-80 hover:opacity-100"
                  }`}
                  onClick={prevImage}
                  aria-label="이전 이미지"
                  disabled={currentImageIndex === 0}
                >
                  <span className="text-gray-800 dark:text-gray-200">&#10094;</span>
                </button>

                <button
                  className={`absolute right-2 top-1/2 transform -translate-y-1/2 bg-white dark:bg-gray-700 bg-opacity-70 dark:bg-opacity-70 rounded-full w-8 h-8 flex items-center justify-center ${
                    currentImageIndex === (feed.imgUrlList?.length ?? 0) - 1
                      ? "opacity-50 cursor-not-allowed"
                      : "opacity-80 hover:opacity-100"
                  }`}
                  onClick={nextImage}
                  aria-label="다음 이미지"
                  disabled={
                    currentImageIndex === (feed.imgUrlList?.length ?? 0) - 1
                  }
                >
                  <span className="text-gray-800 dark:text-gray-200">&#10095;</span>
                </button>
              </>
            )}

            {/* 이미지 인디케이터 (도트) */}
            {hasMultipleImages && (
              <div className="absolute bottom-3 left-0 right-0 flex justify-center space-x-1">
                {feed.imgUrlList?.map((_, idx) => (
                  <span
                    key={idx}
                    className={`inline-block w-2 h-2 rounded-full cursor-pointer ${
                      idx === currentImageIndex ? "bg-blue-500" : "bg-gray-300 dark:bg-gray-600"
                    }`}
                    onClick={(e) => goToImage(idx, e)}
                  ></span>
                ))}
              </div>
            )}

            {/* 이미지 페이지 표시 */}
            {hasMultipleImages && (
              <div className="absolute top-3 right-3 bg-black bg-opacity-60 text-white text-xs px-2 py-1 rounded-full">
                {currentImageIndex + 1} / {feed.imgUrlList?.length ?? 0}
              </div>
            )}
          </div>
        )}

        {/* 액션 버튼 */}
        <div className="flex items-center px-3 pt-3 pb-2">
          <button
            className={`flex items-center mr-4 ${
              isLiked ? "text-red-500" : "text-gray-700 dark:text-gray-300"
            }`}
            onClick={handleLike}
          >
            <span className="text-xl mr-1">{isLiked ? "❤️" : "🤍"}</span>
            <span className="text-sm">{likeCount}</span>
          </button>
          <button
            className="flex items-center mr-4 text-gray-700 dark:text-gray-300 hover:text-blue-500 dark:hover:text-blue-400"
            onClick={handleCommentClick}
          >
            <span className="text-xl mr-1">💬</span>
            <span className="text-sm">{feed.commentCount || 0}</span>
          </button>
          <div className="flex-grow"></div>
          <button
            className={`${isBookmarked ? "text-blue-500" : "text-gray-700 dark:text-gray-300"}`}
            onClick={handleBookmark}
          >
            <span className="text-xl">{!isBookmarked ? "🔖" : "🏷️"}</span>
          </button>
        </div>

        {/* 내용 */}
        <div className="px-3 pt-1 pb-2">
          <p className="text-sm text-gray-800 dark:text-gray-200">
            <span className="font-medium">{feed.authorName}</span>
            <span className="ml-2">{displayContent}</span>
          </p>
          {isContentLong && (
            <button
              className="text-xs text-gray-500 dark:text-gray-400 mt-1"
              onClick={toggleContent}
            >
              {showAllContent ? "접기" : "더 보기"}
            </button>
          )}
        </div>

        {/* 해시태그 */}
        {feed.hashTagList && feed.hashTagList.length > 0 && (
          <div className="px-3 pb-2">
            {feed.hashTagList.map((tag, idx) => (
              <span
                key={idx}
                className="text-blue-500 dark:text-blue-400 text-sm mr-2"
                onClick={(e) => e.stopPropagation()}
              >
                #{tag}
              </span>
            ))}
          </div>
        )}

        {/* 작성 날짜 */}
        <div className="px-3 pb-3">
          <span className="text-xs text-gray-400">{formattedDate}</span>
        </div>
      </div>

      {/* 피드 상세 모달 */}
      {isModalOpen && (
        <FeedDetailModal
          feedId={feed.postId}
          feed={feed} // 현재 피드 데이터 전달
          initialLikeState={isLiked} // 현재 좋아요 상태 전달
          initialBookmarkState={isBookmarked} // 현재 북마크 상태 전달
          onStateChange={handleModalStateChange} // 상태 변경 콜백 전달
          isOpen={isModalOpen}
          onClose={closeModal}
        />
      )}
    </>
  );
};

export default FeedItem;
