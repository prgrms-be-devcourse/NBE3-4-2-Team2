'use client';

import FeedDetailModal from "@/components/feed/FeedDetailModal";
import { useAuth } from "@/contexts/AuthContext";
import { parseAccessToken } from "@/lib/auth/token";
import { components } from '@/lib/backend/apiV1/schema';
import client from '@/lib/backend/client';
import { getImageUrl } from "@/utils/imageUtils";
import { useRouter } from 'next/navigation';
import { useEffect, useRef, useState } from 'react';

type MemberResponse = components['schemas']['MemberResponse'];
type FeedInfoResponse = components["schemas"]["FeedInfoResponse"];
type FeedPost = {
  id: number;
  imageUrl: string;
};

type FollowerMemberDto = components["schemas"]["FollowerMemberDto"];
type FollowerListResponse = components["schemas"]["FollowerListResponse"];

type FollowingMemberDto = components["schemas"]["FollowingMemberDto"];
type FollowingListResponse = components["schemas"]["FollowingListResponse"];

type SearchPostResponse = {
  postId: number;
  imageUrl: string;
};

type SearchPostCursorResponse = {
  searchPostResponses: SearchPostResponse[];
  lastPostId: number | null;
  hasNext: boolean;
};

interface SearchParams {
  type: string;
  keyword: string;
  size: number;
  lastPostId?: number;
}

export default function ProfileClient({ username }: { username: string }) {
  // 컴포넌트 최상위 레벨에서 useAuth 호출
  const { accessToken } = useAuth();

  // 컴포넌트 내부에 라우터 추가
  const router = useRouter();
  
  const [userData, setUserData] = useState<MemberResponse | null>(null);
  const [posts, setPosts] = useState<SearchPostResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [userLoading, setUserLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [lastPostId, setLastPostId] = useState<number | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [followLoading, setFollowLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPostId, setSelectedPostId] = useState<number>(0);
  const [selectedFeed, setSelectedFeed] = useState<FeedInfoResponse | null>(null);
  const [isCurrentUser, setIsCurrentUser] = useState(false);
  const postsContainerRef = useRef<HTMLDivElement | null>(null);
  const pageSize = 12;

  // 팔로워/팔로잉 모달 상태
  const [showFollowersModal, setShowFollowersModal] = useState(false);
  const [showFollowingModal, setShowFollowingModal] = useState(false);
  const [followers, setFollowers] = useState<FollowerMemberDto[]>([]);
  const [following, setFollowing] = useState<FollowingMemberDto[]>([]);
  const [followListLoading, setFollowListLoading] = useState(false);

  // 현재 로그인한 사용자 정보 확인
  useEffect(() => {
    if (accessToken) {
      const { me } = parseAccessToken(accessToken);
      console.log("Current user:", me);
      
      // 현재 로그인한 사용자의 username과 프로필 페이지의 username 비교
      setIsCurrentUser(me.username === username);
    } else {
      // 로그인하지 않은 경우
      setIsCurrentUser(false);
    }
  }, [accessToken, username]);

  // Fetch user data
  useEffect(() => {
    async function fetchUserData() {
      try {
        setUserLoading(true);
        const response = await client.GET('/api-v1/members/{username}', {
          params: {
            path: {
              username
            }
          }
        });

        if (!response.data?.data) {
          throw new Error('사용자 데이터를 가져오는데 실패했습니다');
        }

        setUserData(response.data.data);
        
        // 자신의 프로필이 아닌 경우에만 팔로우 상태 확인
        if (!isCurrentUser) {
          checkFollowStatus(username);
        }
      } catch (error) {
        console.error('Failed to fetch user data:', error);
        setError('사용자 정보를 불러올 수 없습니다');
      } finally {
        setUserLoading(false);
      }
    }

    fetchUserData();
  }, [username, isCurrentUser]);
    // 팔로우 상태 확인 - API 경로 수정
    const checkFollowStatus = async (targetUsername: string) => {
      try {
        const response = await client.GET('/api-v1/member/following/{receiver}', {
          params: {
            path: {
              receiver: targetUsername
            }
          }
        });
        
        if (response.data?.data !== undefined) {
          setIsFollowing(response.data.data.isFollowing);
        }
      } catch (error) {
        console.error('팔로우 상태 확인 중 오류 발생:', error);
      }
    };
  
   // 팔로워 목록 가져오기
const fetchFollowers = async () => {
  if (!userData || followListLoading) return;
  
  setFollowListLoading(true);
  
  try {
    // 프로필 페이지 주인의 팔로워 목록 가져오기
    const response = await client.GET('/api-v1/member/{username}/followers', {
      params: {
        path: {
          username: username // URL에서 전달받은 username 파라미터
        }
      }
    });
    
    if (response.data?.data) {
      const followerData = response.data.data;
      setFollowers(followerData.followerList || []);
      setShowFollowersModal(true);
    }
  } catch (error) {
    console.error('팔로워 목록 가져오기 실패:', error);
  } finally {
    setFollowListLoading(false);
  }
};

// 팔로잉 목록 가져오기
const fetchFollowing = async () => {
  if (!userData || followListLoading) return;
  
  setFollowListLoading(true);
  
  try {
    // 프로필 페이지 주인의 팔로잉 목록 가져오기
    const response = await client.GET('/api-v1/member/{username}/following', {
      params: {
        path: {
          username: username // URL에서 전달받은 username 파라미터
        }
      }
    });
    
    if (response.data?.data) {
      const followingData = response.data.data;
      setFollowing(followingData.followingList || []);
      setShowFollowingModal(true);
    }
  } catch (error) {
    console.error('팔로잉 목록 가져오기 실패:', error);
  } finally {
    setFollowListLoading(false);
  }
};


  
    // 팔로우/언팔로우 처리
    const handleFollowToggle = async () => {
      if (!userData || followLoading) return;
      
      setFollowLoading(true);
      
      try {
        if (isFollowing) {
          // 언팔로우 요청
          await client.DELETE('/api-v1/member/follow/{receiver}', {
            params: {
              path: {
                receiver: userData.username
              }
            }
          });
          
          // 팔로워 수 감소
          if (userData.followerCount && userData.followerCount > 0) {
            setUserData({
              ...userData,
              followerCount: userData.followerCount - 1
            });
          }
        } else {
          // 팔로우 요청
          await client.POST('/api-v1/member/follow/{receiver}', {
            params: {
              path: {
                receiver: userData.username
              }
            }
          });
          
          // 팔로워 수 증가
          setUserData({
            ...userData,
            followerCount: (userData.followerCount || 0) + 1
          });
        }
        
        // 팔로우 상태 토글
        setIsFollowing(!isFollowing);
      } catch (error) {
        console.error('팔로우 처리 중 오류 발생:', error);
      } finally {
        setFollowLoading(false);
      }
    };
  
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
  
    // 포스트 클릭 핸들러
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

    // 사용자 프로필로 이동하는 함수
    const navigateToProfile = (username) => {
      router.push(`/member/${username}`);
    };
  
    // 모달 닫기 함수
    const closeModal = () => {
      setIsModalOpen(false);
      setSelectedPostId(0);
      setSelectedFeed(null);
  
      // 모달이 닫힐 때 body 스크롤 복원
      document.body.style.overflow = "";
    };
  
  // 초기 게시물 로드
  useEffect(() => {
    if (userData && hasMore && posts.length === 0 && !loading) {
      fetchPosts();
    }
  }, [userData]);

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
      container.addEventListener('scroll', handleScroll);
      return () => container.removeEventListener('scroll', handleScroll);
    }
  }, [handleScroll]);

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
        <div className="bg-white dark:bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-md">
          <p className="text-center text-red-500 dark:text-red-400">{error || '사용자 정보를 불러올 수 없습니다'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-24 flex justify-center">
      <div className="bg-white dark:bg-gray-800 p-8 rounded-lg shadow-lg w-full max-w-md flex flex-col">
        {/* 프로필 정보 */}
        <div className="flex flex-col items-center">
          <img
            src={userData?.profileUrl || "https://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http://t1.kakaocdn.net/account_images/default_profile.jpeg"}
            alt="User Avatar"
            className="w-32 h-32 rounded-full mb-4 object-cover"
          />
          <div className="flex items-center gap-4 mb-2">
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{userData?.username}</h1>
            
            {/* 자신의 프로필이 아닐 때만 팔로우 버튼 표시 */}
            {!isCurrentUser && (
              <button
                onClick={handleFollowToggle}
                disabled={followLoading}
                className={`px-4 py-1 rounded-full text-sm font-medium transition-colors ${
                  isFollowing
                    ? 'bg-gray-200 text-gray-800 hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600'
                    : 'bg-blue-500 text-white hover:bg-blue-600 dark:bg-blue-600 dark:hover:bg-blue-700'
                }`}
              >
                {followLoading ? (
                  <span className="inline-block w-4 h-4 border-2 border-gray-300 border-t-blue-500 rounded-full animate-spin"></span>
                ) : isFollowing ? (
                  '언팔로우'
                ) : (
                  '팔로우'
                )}
              </button>
            )}
          </div>
          <div className="flex justify-around text-center text-gray-600 dark:text-gray-300 w-full my-4">
            <div>
              <p className="font-bold text-gray-900 dark:text-white">{userData.postCount || 0}</p>
              <p>Posts</p>
            </div>
            <div 
              className="cursor-pointer hover:opacity-80"
              onClick={fetchFollowers}
            >
              <p className="font-bold text-gray-900 dark:text-white">{userData.followerCount || 0}</p>
              <p>Followers</p>
            </div>
            <div 
              className="cursor-pointer hover:opacity-80"
              onClick={fetchFollowing}
            >
              <p className="font-bold text-gray-900 dark:text-white">{userData.followingCount || 0}</p>
              <p>Following</p>
            </div>
          </div>
        </div>

        {/* 게시물 섹션 */}
        <div className="mt-6">
          <h2 className="text-lg font-bold mb-2 text-gray-900 dark:text-white">Posts</h2>
          <div
            ref={postsContainerRef}
            className="posts-container overflow-y-auto max-h-96 grid grid-cols-3 gap-2"
          >
            {posts.map((post) => (
              <div 
                key={post.postId} 
                className="bg-gray-200 dark:bg-gray-700 p-1 rounded-lg cursor-pointer"
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
              <div className="col-span-3 text-center py-8 text-gray-500 dark:text-gray-400">
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
            <div className="text-center text-gray-500 dark:text-gray-400 mt-4">
              모든 포스트를 불러왔습니다.
            </div>
          )}
        </div>
      </div>

      {/* 피드 상세 모달 */}
      {isModalOpen && selectedFeed && (
        <FeedDetailModal
          feedId={selectedPostId}
          feed={selectedFeed}
          onStateChange={(updatedFeed) => console.log("Feed state updated in modal:", updatedFeed)}
          isOpen={isModalOpen}
          onClose={closeModal}
        />
      )}

      {/* 팔로워 모달 */}
{showFollowersModal && (
  <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
    <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white">팔로워</h3>
        <button 
          onClick={() => setShowFollowersModal(false)}
          className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
      
      {followListLoading ? (
        <div className="flex justify-center py-8">
          <div className="w-8 h-8 border-4 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
        </div>
      ) : followers && followers.length > 0 ? (
        <ul className="divide-y divide-gray-200 dark:divide-gray-700">
          {followers.map(follower => (
            <li 
              key={follower.id} 
              className="py-3 flex items-center cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
              onClick={() => navigateToProfile(follower.username)}
            >
              <img 
                src={follower.profileUrl || "https://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http://t1.kakaocdn.net/account_images/default_profile.jpeg"} 
                alt={follower.username}
                className="w-10 h-10 rounded-full mr-3 object-cover"
              />
              <span className="text-gray-900 dark:text-white font-medium">{follower.username}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-center py-8 text-gray-500 dark:text-gray-400">팔로워가 없습니다.</p>
      )}
    </div>
  </div>
)}

{/* 팔로잉 모달 */}
{showFollowingModal && (
  <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
    <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white">팔로잉</h3>
        <button 
          onClick={() => setShowFollowingModal(false)}
          className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
        >
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
      
      {followListLoading ? (
        <div className="flex justify-center py-8">
          <div className="w-8 h-8 border-4 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
        </div>
      ) : following && following.length > 0 ? (
        <ul className="divide-y divide-gray-200 dark:divide-gray-700">
          {following.map(follow => (
            <li 
              key={follow.id} 
              className="py-3 flex items-center cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
              onClick={() => navigateToProfile(follow.username)}
            >
              <img 
                src={follow.profileUrl || "https://img1.kakaocdn.net/thumb/R640x640.q70/?fname=http://t1.kakaocdn.net/account_images/default_profile.jpeg"} 
                alt={follow.username}
                className="w-10 h-10 rounded-full mr-3 object-cover"
              />
              <span className="text-gray-900 dark:text-white font-medium">{follow.username}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-center py-8 text-gray-500 dark:text-gray-400">팔로잉하는 사용자가 없습니다.</p>
      )}
    </div>
  </div>
)}
</div>
  );
}
