"use client";

import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client";
import { getImageUrl } from "@/utils/imageUtils";
import { useEffect, useRef, useState } from "react";
import FeedDetailModal from "@/components/feed/FeedDetailModal"; // 모달 컴포넌트 import

type MemberResponse = components["schemas"]["MemberResponse"];
type FeedInfoResponse = components["schemas"]["FeedInfoResponse"]; // FeedInfoResponse 타입 추가
type FeedPost = {
  id: number;
  imageUrl: string;
};

type FeedMemberResponse = {
  posts: FeedPost[];
  hasMore: boolean;
};

type SearchPostResponse = {
  postId: number;
  imageUrl: string;
};

type SearchPostCursorResponse = {
  searchPostResponses: SearchPostResponse[];
  lastPostId: number | null;
  hasNext: boolean;
};

// 검색 API 요청을 위한 인터페이스
interface SearchParams {
  type: string;
  keyword: string;
  size: number;
  lastPostId?: number; // lastPostId 속성을 추가
}

export default function ProfileClient({ username }: { username: string }) {
  const [userData, setUserData] = useState<MemberResponse | null>(null);
  const [posts, setPosts] = useState<SearchPostResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [userLoading, setUserLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [lastPostId, setLastPostId] = useState<number | null>(null);
  const postsContainerRef = useRef<HTMLDivElement | null>(null);
  const pageSize = 12; // 한 번에 로드할 게시물 개수

  // 모달 관련 상태 추가
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [selectedPostId, setSelectedPostId] = useState<number | null>(null);
  const [selectedFeed, setSelectedFeed] = useState<FeedInfoResponse | null>(
    null
  );

  // Fetch user data
  useEffect(() => {
    async function fetchUserData() {
      try {
        setUserLoading(true);
        const response = await client.GET("/api-v1/members/{username}", {
          params: {
            path: {
              username,
            },
          },
        });

        if (!response.data?.data) {
          throw new Error("사용자 데이터를 가져오는데 실패했습니다");
        }

        setUserData(response.data.data);
      } catch (error) {
        console.error("Failed to fetch user data:", error);
        setError("사용자 정보를 불러올 수 없습니다");
      } finally {
        setUserLoading(false);
      }
    }

    fetchUserData();
  }, [username]);

  // 게시물 로드 함수
  const fetchPosts = async () => {
    if (!userData || !hasMore || loading) return;

    setLoading(true);

    try {
      const searchParams: SearchParams = {
        type: "AUTHOR",
        keyword: username,
        size: pageSize,
      };

      if (lastPostId !== null) {
        searchParams.lastPostId = lastPostId;
      } else {
        searchParams.lastPostId = 0;
      }

      const params = new URLSearchParams();
      Object.entries(searchParams).forEach(([key, value]) => {
        if (value !== undefined) {
          params.append(key, value.toString());
        }
      });

      const baseUrl = "http://localhost:8080";
      const response = await fetch(`${baseUrl}/api-v1/search?${params}`, {
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error(
          `검색 요청 실패: ${response.status} ${response.statusText}`
        );
      }

      const result = await response.json();

      if (result.success) {
        const data: SearchPostCursorResponse = result.data;
        setPosts((prev) =>
          prev.length === 0
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

  // 초기 게시물 로드
  useEffect(() => {
    if (userData && hasMore && posts.length === 0 && !loading) {
      fetchPosts();
    }
  }, [userData, fetchPosts, hasMore, posts.length]);

  // 스크롤 핸들러
  const handleScroll = () => {
    const container = postsContainerRef.current;
    if (!container || loading || !hasMore) return;

    const { scrollTop, scrollHeight, clientHeight } = container;

    if (scrollHeight - scrollTop - clientHeight < 100) {
      fetchPosts();
    }
  };

  // 스크롤 이벤트 리스너 추가
  useEffect(() => {
    const container = postsContainerRef.current;
    if (container) {
      container.addEventListener("scroll", handleScroll);
      return () => container.removeEventListener("scroll", handleScroll);
    }
  }, [handleScroll]);

  // 포스트 클릭 핸들러 추가
  const handlePostClick = async (postId: number) => {
    try {
      setSelectedPostId(postId);

      // 모달 열기
      setIsModalOpen(true);

      // 모달이 열릴 때 body 스크롤 방지
      document.body.style.overflow = "hidden";

      // 해당 포스트의 상세 정보를 불러옴
      const response = await client.GET("/api-v1/feed/{postId}", {
        params: {
          path: {
            postId: postId,
          },
        },
      });

      if (response.data?.data) {
        setSelectedFeed(response.data.data);
      } else {
        console.error("포스트 정보를 불러오는데 실패했습니다.");
      }
    } catch (error) {
      console.error("포스트 클릭 처리 중 오류:", error);
    }
  };

  // 모달 닫기 함수
  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedPostId(null);
    setSelectedFeed(null);

    // 모달이 닫힐 때 body 스크롤 복원
    document.body.style.overflow = "";
  };

  // 모달에서 상태가 변경되었을 때 호출될 콜백 함수
  const handleModalStateChange = (updatedFeed: FeedInfoResponse) => {
    // 필요한 경우 여기서 피드 상태 업데이트 가능
    console.log("Feed state updated in modal:", updatedFeed);
  };

  if (userLoading) {
    return (
      <div className="container mx-auto py-24 flex justify-center items-center">
        <div className="w-8 h-8 border-4 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
      </div>
    );
  }

  if (error || !userData) {
    return (
      <div className="container mx-auto py-24 flex justify-center">
        <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md">
          <p className="text-center text-red-500">
            {error || "사용자 정보를 불러올 수 없습니다"}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-24 flex justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md flex flex-col">
        {/* 프로필 정보 */}
        <div className="flex flex-col items-center">
          <img
            src={userData.profileUrl || "https://via.placeholder.com/150"}
            alt="User Avatar"
            className="w-32 h-32 rounded-full mb-4"
          />
          <h1 className="text-2xl font-bold text-gray-900">
            {userData.username}
          </h1>
          <div className="flex justify-around text-center text-gray-600 w-full my-4">
            <div>
              <p className="font-bold text-gray-900">
                {userData.postCount || 0}
              </p>
              <p>Posts</p>
            </div>
            <div>
              <p className="font-bold text-gray-900">
                {userData.followerCount || 0}
              </p>
              <p>Followers</p>
            </div>
            <div>
              <p className="font-bold text-gray-900">
                {userData.followingCount || 0}
              </p>
              <p>Following</p>
            </div>
          </div>
        </div>

        {/* 게시물 섹션 */}
        <div className="mt-6">
          <h2 className="text-lg font-bold mb-2 text-gray-900">Posts</h2>
          <div
            ref={postsContainerRef}
            className="posts-container overflow-y-auto max-h-96 grid grid-cols-3 gap-2"
          >
            {posts.map((post) => (
              <div
                key={post.postId}
                className="bg-gray-200 p-1 rounded-lg cursor-pointer"
                onClick={() => handlePostClick(post.postId)}
              >
                <img
                  src={getImageUrl(post.imageUrl)}
                  alt={`Post ${post.postId}`}
                  className="w-full h-40 object-cover rounded-md hover:opacity-80 transition"
                />
              </div>
            ))}

            {posts.length === 0 && !loading && (
              <div className="col-span-3 text-center py-8 text-gray-500">
                게시물이 없습니다.
              </div>
            )}
          </div>

          {loading && (
            <div className="flex justify-center py-4">
              <div className="w-6 h-6 border-4 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
            </div>
          )}

          {!hasMore && posts.length > 0 && (
            <div className="text-center text-gray-500 mt-4">
              모든 포스트를 불러왔습니다.
            </div>
          )}
        </div>
      </div>

      {/* 피드 상세 모달 */}
      {isModalOpen && selectedPostId && selectedFeed && (
        <FeedDetailModal
          feedId={selectedPostId}
          feed={selectedFeed}
          onStateChange={handleModalStateChange}
          isOpen={isModalOpen}
          onClose={closeModal}
        />
      )}
    </div>
  );
}
