/* eslint-disable @next/next/no-img-element */
"use client";

import { useState, useRef } from "react";
import { useRouter } from "next/navigation";
import { components } from "../../lib/backend/apiV1/schema";
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
  const router = useRouter();
  const [isLiked, setIsLiked] = useState<boolean>(false);
  const [isBookmarked, setIsBookmarked] = useState<boolean>(!!feed.bookmarkId);
  const [showAllContent, setShowAllContent] = useState<boolean>(false);
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);

  // 슬라이더 참조
  const sliderRef = useRef<HTMLDivElement>(null);

  // 좋아요 기능
  const handleLike = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    setIsLiked(!isLiked);
    console.log(isLiked ? "좋아요를 누릅니다." : "좋아요를 취소합니다.");

    // API 호출은 여기에 구현
  };

  // 북마크 기능
  const handleBookmark = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지

    console.log(isBookmarked ? "북마크를 추가합니다." : "북마크를 취소합니다.");
    setIsBookmarked(!isBookmarked);
    // API 호출은 여기에 구현
  };

  // 댓글 버튼 클릭 시 상세 페이지로 이동
  const handleCommentClick = (e: React.MouseEvent): void => {
    e.stopPropagation(); // 이벤트 전파 중지
    navigateToDetail();
  };

  // 피드 아이템 클릭 시 상호작용 시 이곳에서 처리
  const handleFeedClick = (): void => {
    //navigateToDetail();
  };

  // 상세 페이지 이동 함수
  const navigateToDetail = (): void => {
    console.log("상세 페이지로 이동합니다.");
    router.push(`/feed/${feed.postId}`);
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
    <div
      className={`bg-white rounded-lg shadow-sm mb-4 overflow-hidden ${
        isActive ? "border-blue-400 border-2" : ""
      }`}
      onClick={handleFeedClick}
    >
      {/* 작성자 정보 */}
      <div className="flex items-center justify-between p-3">
        <div className="flex items-center" onClick={handleProfileImage}>
          <div className="w-8 h-8 rounded-full bg-gray-200 flex-shrink-0"></div>
          <span className="ml-2 font-medium text-sm">{feed.authorName}</span>
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
                  src={imgUrl}
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
                className={`absolute left-2 top-1/2 transform -translate-y-1/2 bg-white bg-opacity-70 rounded-full w-8 h-8 flex items-center justify-center ${
                  currentImageIndex === 0
                    ? "opacity-50 cursor-not-allowed"
                    : "opacity-80 hover:opacity-100"
                }`}
                onClick={prevImage}
                aria-label="이전 이미지"
                disabled={currentImageIndex === 0}
              >
                <span className="text-gray-800">&#10094;</span>
              </button>

              <button
                className={`absolute right-2 top-1/2 transform -translate-y-1/2 bg-white bg-opacity-70 rounded-full w-8 h-8 flex items-center justify-center ${
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
                <span className="text-gray-800">&#10095;</span>
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
                    idx === currentImageIndex ? "bg-blue-500" : "bg-gray-300"
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
            isLiked ? "text-red-500" : "text-gray-700"
          }`}
          onClick={handleLike}
        >
          <span className="text-xl mr-1">{isLiked ? "❤️" : "🤍"}</span>
          <span className="text-sm">{feed.likeCount || 0}</span>
        </button>
        <button
          className="flex items-center mr-4 text-gray-700 hover:text-blue-500"
          onClick={handleCommentClick}
        >
          <span className="text-xl mr-1">💬</span>
          <span className="text-sm">{feed.commentCount || 0}</span>
        </button>
        <div className="flex-grow"></div>
        <button
          className={`${isBookmarked ? "text-blue-500" : "text-gray-700"}`}
          onClick={handleBookmark}
        >
          <span className="text-xl">{isBookmarked ? "🔖" : "🏷️"}</span>
        </button>
      </div>

      {/* 내용 */}
      <div className="px-3 pt-1 pb-2">
        <p className="text-sm text-gray-800">
          <span className="font-medium">{feed.authorName}</span>
          <span className="ml-2">{displayContent}</span>
        </p>
        {isContentLong && (
          <button
            className="text-xs text-gray-500 mt-1"
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
              className="text-blue-500 text-sm mr-2"
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
  );
};

export default FeedItem;
