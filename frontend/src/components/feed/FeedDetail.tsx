"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { dummyFeeds } from "@/components/feed/dummyData";
import { components } from "../../lib/backend/apiV1/schema";

type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];

export default function FeedDetail({ id }: { id: string }) {
  const router = useRouter();
  const feedId = parseInt(id);
  const [feed, setFeed] = useState<FeedInfoResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isLiked, setIsLiked] = useState<boolean>(false);
  const [isBookmarked, setIsBookmarked] = useState<boolean>(false);
  const [currentImageIndex, setCurrentImageIndex] = useState<number>(0);
  const [newComment, setNewComment] = useState<string>("");

  // 가상의 댓글 데이터
  const [comments] = useState([
    {
      id: 1,
      username: "이지은",
      content:
        "새로 만 카페에서 좋은 사진들이 아직 올라가지 않았네요. 일본의 골목길 모습이 정말 잘 담겼네요.",
      time: "6일 전",
      likes: 0,
    },
    {
      id: 2,
      username: "Marcus Hall",
      content:
        "Great composition! I love the lighting in this shot. Was this your first trip to Japan? You did an excellent job of capturing the atmosphere.",
      time: "2 hours",
      likes: 2,
    },
    {
      id: 3,
      username: "Dianne Russell",
      content:
        "But don't you think the timing is off because many other apps have done this even earlier, causing people to switch apps?",
      time: "53 min",
      likes: 1,
    },
    {
      id: 4,
      username: "Esther Howard",
      content:
        "This could be due to them taking their time to release a stable version.",
      time: "32 min",
      likes: 12,
    },
    {
      id: 5,
      username: "You",
      content: "",
      time: "Just now",
      likes: 0,
    },
  ]);

  // 이미지가 있는지 확인
  const hasImages = feed?.imgUrlList && feed.imgUrlList.length > 0;

  useEffect(() => {
    // 단일 피드 정보 불러오기
    const fetchFeedDetail = () => {
      setLoading(true);

      try {
        console.log(`피드 ID: ${feedId} 데이터 불러오는 중...`);

        // 실제 구현에서는 API 호출로 대체
        // 여기서는 더미 데이터에서 해당 ID의 피드를 찾음
        const foundFeed =
          dummyFeeds.feedList?.find((feed) => feed.postId === feedId) || null;

        if (foundFeed) {
          console.log("피드를 찾았습니다:", foundFeed);
          setFeed(foundFeed);
          setIsBookmarked(!!foundFeed.bookmarkId);
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
  }, [feedId]);

  // 뒤로가기 처리
  const handleGoBack = () => {
    // 브라우저의 뒤로가기를 사용하여 이전 페이지로 이동
    router.back();
  };

  // 좋아요 기능
  const handleLike = (): void => {
    setIsLiked(!isLiked);
    // API 호출은 여기에 구현
  };

  // 북마크 기능
  const handleBookmark = (): void => {
    setIsBookmarked(!isBookmarked);
    // API 호출은 여기에 구현
  };

  // 댓글 추가 기능
  const handleAddComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    // API 호출로 댓글 저장
    setNewComment("");
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
      <div className="max-w-6xl mx-auto my-6 flex">
        {/* 피드 콘텐츠 */}
        <div className="flex-1 flex flex-col md:flex-row bg-black">
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
                <div className="w-full aspect-square bg-black flex items-center justify-center">
                  <img
                    src={feed.imgUrlList?.[currentImageIndex]}
                    alt="피드 이미지"
                    className="max-h-full max-w-full object-contain"
                  />
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
                <button className="mr-4">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    className="h-6 w-6"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
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
                  {feed.likesCount || 0} 좋아요
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
          <div className="w-full md:w-[420px] border-l border-gray-800 flex flex-col">
            {/* 댓글 헤더 */}
            <div className="p-4 border-b border-gray-800">
              <h2 className="text-lg font-medium">댓글</h2>
            </div>

            {/* 댓글 목록 스크롤 영역 */}
            <div className="flex-1 overflow-y-auto">
              {comments.map((comment) => (
                <div key={comment.id} className="p-4 border-b border-gray-800">
                  <div className="flex">
                    <div className="w-8 h-8 rounded-full bg-gray-700 mr-3"></div>
                    <div className="flex-1">
                      <div className="flex justify-between mb-1">
                        <span className="font-medium text-sm">
                          {comment.username}
                        </span>
                        <span className="text-xs text-gray-500">
                          {comment.time}
                        </span>
                      </div>
                      <p className="text-sm text-gray-300 mb-2">
                        {comment.content}
                      </p>
                      <div className="flex items-center space-x-4 text-xs text-gray-500">
                        <span>{comment.likes} 좋아요</span>
                        <button className="hover:text-white">답글</button>
                      </div>
                    </div>
                    {comment.likes > 0 && (
                      <button className="ml-2 text-gray-500">
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          className="h-4 w-4"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L7 11v9m7-10h-2M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5"
                          />
                        </svg>
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* 댓글 입력 */}
            <div className="p-4 border-t border-gray-800">
              <form onSubmit={handleAddComment} className="flex items-center">
                <input
                  type="text"
                  placeholder="댓글을 입력하세요..."
                  className="flex-grow bg-transparent border-0 focus:ring-0 text-sm text-white placeholder-gray-500"
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                />
                <button
                  type="submit"
                  disabled={!newComment.trim()}
                  className={`text-sm font-medium ${
                    newComment.trim() ? "text-blue-400" : "text-blue-800"
                  }`}
                >
                  게시
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
