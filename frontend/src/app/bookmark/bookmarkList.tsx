"use client";

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import Image from 'next/image';

// 북마크 아이템 타입 정의
interface BookmarkItem {
  bookmarkId: number;
  postId: number;
  postContent: string;
  imageUrls: string[];
  bookmarkedAt: string;
}

// API 응답 타입 정의
interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
}

export default function BookmarkList() {
  const [bookmarks, setBookmarks] = useState<BookmarkItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const [page, setPage] = useState<number>(1);
  const [deleteModal, setDeleteModal] = useState<{
    show: boolean;
    bookmarkId: number | null;
    postId: number | null;
  }>({ show: false, bookmarkId: null, postId: null });
  const observerRef = useRef<IntersectionObserver | null>(null);
  const loadingRef = useRef<HTMLDivElement>(null);
  const router = useRouter();
  
  const ITEMS_PER_PAGE = page === 1 ? 20 : 10;

  // 모든 북마크 데이터
  const [allBookmarks, setAllBookmarks] = useState<BookmarkItem[]>([]);
  
  // 북마크 목록 가져오기 (모든 데이터 한 번에 로드)
  const fetchAllBookmarks = async () => {
    try {
      setLoading(true);
      
      const response = await fetch('/api-v1/bookmark/list', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}` // 토큰 가져오기
        }
      });

      if (!response.ok) {
        throw new Error('북마크를 가져오는데 실패했습니다.');
      }

      const result: ApiResponse<BookmarkItem[]> = await response.json();
      
      // 모든 북마크 데이터 저장
      setAllBookmarks(result.data);
      
      // 첫 페이지 데이터 설정 (처음 20개)
      const firstPageItems = result.data.slice(0, ITEMS_PER_PAGE);
      setBookmarks(firstPageItems);
      
      // 더 불러올 데이터가 있는지 확인
      setHasMore(result.data.length > ITEMS_PER_PAGE);
    } catch (err) {
      setError(err instanceof Error ? err.message : '북마크를 가져오는데 오류가 발생했습니다.');
      console.error('북마크 목록 가져오기 오류:', err);
    } finally {
      setLoading(false);
    }
  };
  
  // 특정 페이지의 데이터 로드 (클라이언트 측 페이지네이션)
  const loadMoreItems = () => {
    const nextItems = allBookmarks.slice(
      bookmarks.length, 
      bookmarks.length + (page === 1 ? 20 : 10)
    );
    
    if (nextItems.length > 0) {
      setBookmarks(prev => [...prev, ...nextItems]);
      setHasMore(bookmarks.length + nextItems.length < allBookmarks.length);
    } else {
      setHasMore(false);
    }
  };

  // 초기 로딩
  useEffect(() => {
    fetchAllBookmarks();
  }, []);

  // Intersection Observer 설정
  useEffect(() => {
    // 이전 Observer 해제
    if (observerRef.current) {
      observerRef.current.disconnect();
    }

    observerRef.current = new IntersectionObserver(
      entries => {
        const [entry] = entries;
        if (entry.isIntersecting && hasMore && !loading) {
          setPage(prevPage => prevPage + 1);
        }
      },
      { threshold: 0.5 }
    );

    if (loadingRef.current) {
      observerRef.current.observe(loadingRef.current);
    }

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [hasMore, loading]);

  // 페이지 변경 시 데이터 로드 (클라이언트 측 페이지네이션)
  useEffect(() => {
    if (page > 1) {
      loadMoreItems();
    }
  }, [page]);

  // 북마크 삭제 함수
  const handleDeleteBookmark = async (bookmarkId: number, postId: number) => {
    try {
      const response = await fetch(`/api-v1/bookmark/${postId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ bookmarkId })
      });

      if (!response.ok) {
        throw new Error('북마크 삭제에 실패했습니다.');
      }

      // 성공적으로 삭제하면 목록에서 제거 (표시되는 북마크와 전체 북마크 목록 모두에서 제거)
      setBookmarks(prevBookmarks => prevBookmarks.filter(bookmark => bookmark.bookmarkId !== bookmarkId));
      setAllBookmarks(prevAllBookmarks => prevAllBookmarks.filter(bookmark => bookmark.bookmarkId !== bookmarkId));
      
      // 모달 닫기
      setDeleteModal({ show: false, bookmarkId: null, postId: null });
    } catch (err) {
      console.error('북마크 삭제 오류:', err);
      alert(err instanceof Error ? err.message : '북마크 삭제 중 오류가 발생했습니다.');
    }
  };

  // 삭제 모달 열기
  const openDeleteModal = (e: React.MouseEvent, bookmarkId: number, postId: number) => {
    e.stopPropagation();
    setDeleteModal({ show: true, bookmarkId, postId });
  };

  // 게시물로 이동 함수
  const navigateToPost = (postId: number) => {
    router.push(`/post/${postId}`);
  };

  if (loading && page === 1) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error && bookmarks.length === 0) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative" role="alert">
        <strong className="font-bold">오류 발생!</strong>
        <span className="block sm:inline"> {error}</span>
      </div>
    );
  }

  if (bookmarks.length === 0) {
    return (
      <div className="text-center py-10">
        <p className="text-lg text-gray-500">북마크한 게시물이 없습니다.</p>
        <button 
          onClick={() => router.push('/')} 
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
        >
          게시물 둘러보기
        </button>
      </div>
    );
  }

  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {bookmarks.map((bookmark) => (
          <div 
            key={bookmark.bookmarkId} 
            className="bg-white dark:bg-gray-800 border dark:border-gray-700 rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
            onClick={() => navigateToPost(bookmark.postId)}
          >
            <div className="relative h-48 w-full bg-gray-200 dark:bg-gray-700">
              {bookmark.imageUrls && bookmark.imageUrls.length > 0 ? (
                <Image
                  src={bookmark.imageUrls[0]}
                  alt="게시물 이미지"
                  fill
                  sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
                  style={{ objectFit: 'cover' }}
                  priority={bookmark.bookmarkId <= 6} // 처음 몇 개의 이미지만 priority 설정
                />
              ) : (
                <div className="flex justify-center items-center h-full">
                  <span className="text-gray-400 dark:text-gray-500">이미지 없음</span>
                </div>
              )}
              {bookmark.imageUrls && bookmark.imageUrls.length > 1 && (
                <div className="absolute bottom-2 right-2 bg-black bg-opacity-70 text-white text-xs px-2 py-1 rounded">
                  +{bookmark.imageUrls.length - 1}
                </div>
              )}
            </div>
            
            <div className="p-4">
              <p className="text-gray-700 dark:text-gray-300 line-clamp-3 mb-2">{bookmark.postContent}</p>
              <div className="flex justify-between items-center">
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  {new Date(bookmark.bookmarkedAt).toLocaleDateString()}
                </p>
                <button
                  onClick={(e) => openDeleteModal(e, bookmark.bookmarkId, bookmark.postId)}
                  className="text-gray-500 hover:text-red-500 transition-colors p-1"
                  aria-label="북마크 삭제"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
      
      {/* 추가 로딩 표시기 */}
      {hasMore && (
        <div ref={loadingRef} className="flex justify-center items-center py-6">
          {loading && (
            <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
          )}
          {!loading && <p className="text-gray-500 dark:text-gray-400">스크롤하여 더 불러오기</p>}
        </div>
      )}
      
      {/* 삭제 확인 모달 */}
      {deleteModal.show && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white dark:bg-gray-800 rounded-lg p-6 max-w-sm w-full mx-4 shadow-xl">
            <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
              북마크 삭제 확인
            </h3>
            <p className="text-gray-700 dark:text-gray-300 mb-6">
              정말로 북마크에서 제거하시겠습니까?
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setDeleteModal({ show: false, bookmarkId: null, postId: null })}
                className="px-4 py-2 bg-gray-200 dark:bg-gray-700 text-gray-800 dark:text-white rounded hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
              >
                취소
              </button>
              <button
                onClick={() => {
                  if (deleteModal.bookmarkId !== null && deleteModal.postId !== null) {
                    handleDeleteBookmark(deleteModal.bookmarkId, deleteModal.postId);
                  }
                }}
                className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}