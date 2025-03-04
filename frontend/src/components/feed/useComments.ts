// useComments.ts
import { useState, useCallback } from 'react';
import { dummyComments, Comment } from './dummyComments';

export function useComments(feedId: number) {
  const [comments, setComments] = useState<Comment[]>(dummyComments);
  
  // 댓글 목록 가져오기 (실제 구현에서는 API 호출)
  const fetchComments = useCallback(async () => {
    // 여기에 실제 API 호출 로직 구현
    // 예: const response = await api.get(`/posts/${feedId}/comments`);
    // setComments(response.data);
    
    // 현재는 더미 데이터를 사용하고 최신순으로 정렬
    // 댓글 데이터의 time 필드를 날짜 객체로 변환하여 정렬하는 것이 정확하지만
    // 현재 예시 데이터의 형식(시간이 문자열)으로 인해 임시로 역순 정렬
    setComments([...dummyComments].reverse());
  }, [feedId]);

  // 새 댓글 추가하기
  const addComment = useCallback((content: string) => {
    if (!content.trim()) return;

    // 새 댓글 객체 생성
    const newCommentObj: Comment = {
      id: Math.max(...comments.map(c => c.id), 0) + 1, // 고유 ID 생성
      username: "You",
      content: content,
      time: "Just now",
      likes: 0,
    };
    
    // 실제 구현에서는 API 호출 후 응답으로 업데이트
    // const response = await api.post(`/posts/${feedId}/comments`, { content });
    // const newCommentFromServer = response.data;
    // setComments(prevComments => [...prevComments, newCommentFromServer]);
    
    // 현재는 로컬 상태만 업데이트 (새 댓글을 맨 위에 추가)
    setComments(prevComments => [newCommentObj, ...prevComments]);
  }, [comments, feedId]);

  // 댓글에 좋아요 추가/취소하기
  const likeComment = useCallback((commentId: number) => {
    // 실제 구현에서는 API 호출 후 응답으로 업데이트
    /*
      // 예시 1: 토글 방식 좋아요
      const response = await api.post(`/comments/${commentId}/like`);
      const updatedComment = response.data;
      
      // 예시 2: 좋아요/좋아요 취소를 구분하는 경우
      const comment = comments.find(c => c.id === commentId);
      if (comment) {
        if (comment.isLikedByMe) {
          // 좋아요 취소 API 호출
          await api.delete(`/comments/${commentId}/like`);
        } else {
          // 좋아요 API 호출
          await api.post(`/comments/${commentId}/like`);
        }
      }
    */
    
    // 현재는 로컬 상태만 업데이트 (단순 좋아요 수 증가)
    setComments(prevComments => 
      prevComments.map(comment => 
        comment.id === commentId
          ? { ...comment, likes: comment.likes + 1 }
          : comment
      )
    );
  }, []);

  // 댓글에 답글 달기
  const replyToComment = useCallback((commentId: number, content: string) => {
    if (!content.trim()) return;
    
    // 실제 구현에서는 답글 관련 API 호출
    /*
      const response = await api.post(`/comments/${commentId}/replies`, { content });
      const newReply = response.data;
    */
    
    // 현재 구현에서는 답글을 새로운 댓글로 추가 (부모 댓글 ID는 무시됨)
    const newReplyObj: Comment = {
      id: Math.max(...comments.map(c => c.id), 0) + 1,
      username: "You",
      content: `@${comments.find(c => c.id === commentId)?.username || ''} ${content}`,
      time: "Just now",
      likes: 0,
    };
    
    // 답글도 최신순으로 맨 위에 추가
    setComments(prevComments => [newReplyObj, ...prevComments]);
  }, [comments]);

  return {
    comments,
    fetchComments,
    addComment,
    likeComment,
    replyToComment
  };
}