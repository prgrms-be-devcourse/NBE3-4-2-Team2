// 스토리지 키 접두사 
const LIKE_STORAGE_KEY = 'feed_likes';
const BOOKMARK_STORAGE_KEY = 'feed_bookmarks';

// 타임스탬프 포함 - 시간 기반 무효화를 위한 구조
interface LikeStatus {
  isLiked: boolean;
  likeCount: number;
  timestamp: number;
}

interface BookmarkStatus {
  isBookmarked: boolean;
  bookmarkId: number; 
  timestamp: number;
}

// 좋아요 상태 저장
export const saveLikeStatus = (feedId: number, isLiked: boolean, likeCount: number): void => {
  try {
    const status: LikeStatus = {
      isLiked,
      likeCount,
      timestamp: Date.now()
    };
    localStorage.setItem(`${LIKE_STORAGE_KEY}_${feedId}`, JSON.stringify(status));
  } catch (error) {
    console.error('좋아요 상태 저장 중 오류:', error);
  }
};

// 북마크 상태 저장
export const saveBookmarkStatus = (feedId: number, isBookmarked: boolean, bookmarkId: number): void => {
  try {
    const status: BookmarkStatus = {
      isBookmarked,
      bookmarkId,
      timestamp: Date.now()
    };
    localStorage.setItem(`${BOOKMARK_STORAGE_KEY}_${feedId}`, JSON.stringify(status));
  } catch (error) {
    console.error('북마크 상태 저장 중 오류:', error);
  }
};

// 좋아요 상태 가져오기
export const getLikeStatus = (feedId: number, serverLikeStatus: boolean, serverLikeCount: number): { isLiked: boolean, likeCount: number } => {
  try {
    const stored = localStorage.getItem(`${LIKE_STORAGE_KEY}_${feedId}`);
    
    if (!stored) {
      return { isLiked: serverLikeStatus, likeCount: serverLikeCount };
    }
    
    const parsedStatus: LikeStatus = JSON.parse(stored);
    
    // 30초 이상 지난 데이터는 서버 데이터 우선 사용 (시간 기반 무효화)
    const isExpired = Date.now() - parsedStatus.timestamp > 30000; // 30초
    
    if (isExpired) {
      return { isLiked: serverLikeStatus, likeCount: serverLikeCount };
    }
    
    return { 
      isLiked: parsedStatus.isLiked, 
      likeCount: parsedStatus.likeCount 
    };
  } catch (error) {
    console.error('좋아요 상태 불러오기 중 오류:', error);
    return { isLiked: serverLikeStatus, likeCount: serverLikeCount };
  }
};

// 북마크 상태 가져오기
export const getBookmarkStatus = (feedId: number, serverBookmarkId: number): { isBookmarked: boolean, bookmarkId: number } => {
  try {
    const stored = localStorage.getItem(`${BOOKMARK_STORAGE_KEY}_${feedId}`);
    
    if (!stored) {
      return { 
        isBookmarked: serverBookmarkId !== -1, 
        bookmarkId: serverBookmarkId 
      };
    }
    
    const parsedStatus: BookmarkStatus = JSON.parse(stored);
    
    // 30초 이상 지난 데이터는 서버 데이터 우선 사용
    const isExpired = Date.now() - parsedStatus.timestamp > 30000; // 30초
    
    if (isExpired) {
      return { 
        isBookmarked: serverBookmarkId !== -1, 
        bookmarkId: serverBookmarkId 
      };
    }
    
    return { 
      isBookmarked: parsedStatus.isBookmarked, 
      bookmarkId: parsedStatus.bookmarkId 
    };
  } catch (error) {
    console.error('북마크 상태 불러오기 중 오류:', error);
    return { 
      isBookmarked: serverBookmarkId !== -1, 
      bookmarkId: serverBookmarkId 
    };
  }
};

// 일괄 업데이트 및 동기화를 위한 함수
export const syncLikeStatuses = (feeds: any[]): any[] => {
  return feeds.map(feed => {
    const { isLiked, likeCount } = getLikeStatus(
      feed.postId, 
      !!feed.likeFlag, 
      feed.likeCount || 0
    );
    
    const { isBookmarked, bookmarkId } = getBookmarkStatus(
      feed.postId,
      feed.bookmarkId
    );
    
    return {
      ...feed,
      likeFlag: isLiked,
      likeCount: likeCount,
      bookmarkId: bookmarkId
    };
  });
};

// 좋아요 데이터 청소 (선택적)
export const cleanupLikeData = (maxAge = 60 * 60 * 1000): void => { // 기본 1시간
  try {
    const now = Date.now();
    
    // localStorage에서 모든 키 가져오기
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      
      if (key && (key.startsWith(LIKE_STORAGE_KEY) || key.startsWith(BOOKMARK_STORAGE_KEY))) {
        const value = localStorage.getItem(key);
        if (value) {
          const data = JSON.parse(value);
          
          // 특정 시간보다 오래된 데이터 삭제
          if (now - data.timestamp > maxAge) {
            localStorage.removeItem(key);
          }
        }
      }
    }
  } catch (error) {
    console.error('좋아요 데이터 정리 중 오류:', error);
  }
};