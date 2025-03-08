// useComments.ts
import { useState, useCallback } from "react";
import client from "@/lib/backend/client";
import { getCurrentUserId } from "../../utils/jwtUtils";

export interface Comment {
  id: number;
  content: string;
  username?: string;
  likeCount?: number;
  parentId?: number | null;
  replies?: Comment[]; // 대댓글 목록
}

export function useComments(feedId: number) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [replies, setReplies] = useState<Comment[]>([]);
  const [error, setError] = useState<string | null>(null);

  // 댓글 목록 가져오기 (실제 구현에서는 API 호출)
  const fetchComments = useCallback(async () => {
    try {
      const response = await client.GET("/api-v1/comment/post/{postId}", {
        params: {
          path: { postId: feedId },
        },
      });
      if (response?.data?.content) {
        const list = response.data.content as Comment[];
        setComments(list);
      } else {
        setComments([]); // 댓글이 없더라도 에러 메시지 설정하지 않음
      }
    } catch (err) {
      console.error("댓글 가져오기 실패", err);
      setError("댓글을 가져오는 데 실패했습니다.");
    }
  }, [feedId]);

  // 새 댓글 추가하기
  const addComment = useCallback(
    async (content: string) => {
      if (!content.trim()) return;

      try {
        const userId = getCurrentUserId();
        // 댓글 추가를 위한 API 호출
        const response = await client.POST("/api-v1/comment", {
          params: {},
          body: {
            postId: feedId,
            content: content,
            memberId: userId,
            parentId: null, // 최상위 댓글이므로 null
          },
        });
        // 서버에서 응답 받은 새로운 댓글이 있을 경우
        if (response?.data) {
          const newComment = response.data as Comment;
          setComments((prevComments) => [response.data, ...prevComments]);
          //현재 ID는 잘 넘어가는데 처음 추가될 때 맨 위에 사용자 ID가 표시되지가 않는거같음
        } else {
          setError("댓글을 추가하는 데 실패했습니다.");
        }
      } catch (err) {
        console.error("댓글 추가 실패", err);
        setError("댓글을 추가하는 데 실패했습니다.");
      }
    },
    [feedId]
  );

  // 대댓글 목록 가져오기
  const fetchReplies = useCallback(async (parentId: number) => {
    try {
      const response = await client.GET("/api-v1/comment/replies/{parentId}", {
        params: {
          path: { parentId },
        },
      });
      if (response?.data?.content) {
        const list = response.data.content as Comment[]; // 대댓글 데이터를 상태에 저장
        setComments(list);
      } else {
        setReplies([]);
      }
    } catch (err) {
      console.error("대댓글 가져오기 실패", err);
      setError("대댓글을 가져오는 데 실패했습니다.");
    }
  }, []);

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
  const addReply = useCallback(
    async (parentId: number, content: string) => {
      if (!content.trim()) return;

      try {
        const userId = getCurrentUserId();
        const response = await client.POST("/api-v1/comment", {
          body: {
            postId: feedId,
            content: content,
            memberId: userId,
            parentId: parentId, // 부모 댓글 ID
          },
        });

        console.log("대댓글 추가 응답:", response);

        if (response?.data) {
          const newReply = response.data as Comment;
          setComments((prevComments) =>
            prevComments.map((c) =>
              c.id === parentId
                ? {
                    ...c,
                    // [CHANGED] replies가 없을 수도 있으므로 (c.replies || [])
                    replies: [newReply, ...(c.replies || [])],
                  }
                : c
            )
          );
        } else {
          setError("대댓글을 추가하는 데 실패했습니다.");
        }
      } catch (error) {
        console.error("대댓글 추가 실패", error);
        setError("대댓글을 추가하는 데 실패했습니다.");
      }
    },
    [feedId]
  );

  return {
    comments,
    replies,
    fetchComments,
    addComment,
    addReply,
    fetchReplies,
    likeComment,
    error,
  };
}
