// useComments.ts
import { useState, useCallback } from "react";
import client from "@/lib/backend/client";
import { paths } from "@/lib/backend/apiV1/schema";

export function useComments(feedId: number) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [error, setError] = useState<string | null>(null);

  // 댓글 목록 가져오기 (실제 구현에서는 API 호출)
  const fetchComments = useCallback(async () => {
    try {
      const response = await client.GET(`/api-v1/comment/post/${feedId}`);

      if (response?.data?.content) {
        setComments(response.data.content);
      } else {
        setComments([]); // 댓글이 없더라도 에러 메시지 설정하지 않음
      }
    } catch (error) {
      console.error("댓글 가져오기 실패", error);
      setError("댓글을 가져오는 데 실패했습니다.");
    }
  }, [feedId]);

  // 새 댓글 추가하기
  const addComment = useCallback(
    async (content: string) => {
      if (!content.trim()) return;

      try {
        // 댓글 추가를 위한 API 호출
        const response = await client.POST("/api-v1/comment", {
          body: {
            postId: feedId, // 댓글을 달 게시물의 ID
            content: content, // 사용자가 작성한 댓글 내용
            memberId: 1, // 로그인한 사용자의 ID (실제 구현에서는 로그인 상태에서 가져옴)
          },
        });

        // 서버에서 응답 받은 새로운 댓글이 있을 경우
        if (response?.data) {
          // 새 댓글을 맨 위에 추가
          setComments((prevComments) => [response.data, ...prevComments]);
        } else {
          setError("댓글을 추가하는 데 실패했습니다.");
        }
      } catch (error) {
        console.error("댓글 추가 실패", error);
        setError("댓글을 추가하는 데 실패했습니다.");
      }
    },
    [feedId]
  );
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
    setComments((prevComments) =>
      prevComments.map((comment) =>
        comment.id === commentId
          ? { ...comment, likeCount: comment.likeCount + 1 }
          : comment
      )
    );
  }, []);

  // 댓글에 답글 달기
  const replyToComment = useCallback(
    (commentId: number, content: string) => {
      if (!content.trim()) return;

      // 실제 구현에서는 답글 관련 API 호출
      /*
      const response = await api.post(`/comments/${commentId}/replies`, { content });
      const newReply = response.data;
    */

      // 현재 구현에서는 답글을 새로운 댓글로 추가 (부모 댓글 ID는 무시됨)
      const newReplyObj: Comment = {
        id: Math.max(...comments.map((c) => c.id), 0) + 1,
        username: "You",
        content: `@${
          comments.find((c) => c.id === commentId)?.username || ""
        } ${content}`,
        time: "Just now",
        likeCount: 0,
      };

      // 답글도 최신순으로 맨 위에 추가
      setComments((prevComments) => [newReplyObj, ...prevComments]);
    },
    [comments]
  );

  return {
    comments,
    fetchComments,
    addComment,
    likeComment,
    replyToComment,
  };
}
