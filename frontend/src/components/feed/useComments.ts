// useComments.ts
import { useState, useCallback } from "react";
import client from "@/lib/backend/client";
import { getCurrentUserId } from "../../utils/jwtUtils";

export interface Comment {
  id: number;
  content: string;
  username?: string;
  memberId: number;
  likeCount?: number;
  parentNum?: number | null;
  replies?: Comment[]; // 대댓글 목록

  answerNum?: number;
}

export function useComments(feedId: number) {
  const [comments, setComments] = useState<Comment[]>([]);
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
        const tree = buildCommentTree(list);
        setComments(tree);
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
            parentNum: null, // 최상위 댓글이므로 null
          },
        });
        // 서버에서 응답 받은 새로운 댓글이 있을 경우
        if (response?.data) {
          const newComment = response.data as Comment;
          setComments((prev) => [newComment, ...prev]);
          //현재 ID는 잘 넘어가는데 처음 추가될 때 맨 위에 사용자 ID가 표시되지가 않는거같음
        }
      } catch (err) {
        console.error("댓글 추가 실패", err);
        setError("댓글을 추가하는 데 실패했습니다.");
      }
    },
    [feedId]
  );

  // 대댓글 달기
  const replyToComment = useCallback(
    async (parentNum: number, content: string) => {
      if (!content.trim()) return;

      try {
        const userId = getCurrentUserId();
        const response = await client.POST("/api-v1/comment", {
          body: {
            postId: feedId,
            content,
            memberId: userId,
            parentNum,
          },
        });
        
        if (response?.data) {
          const newReply = response.data as Comment;
          setComments((prev) =>
            insertReplyRecursively(prev, parentNum, newReply)
          );
        }
      } catch (err) {
        console.error("대댓글 추가 실패", err);
        setError("대댓글을 추가하는 데 실패했습니다.");
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

  const loadMoreReplies = useCallback(async (parentNum: number, page = 0) => {
    try {
      // 예: /api-v1/comment/replies/{parentNum}?page=...&size=5
      const response = await client.GET("/api-v1/comment/replies/{parentNum}", {
        params: {
          path: { parentNum },
          query: { page, size: 5 },
        },
      });
      if (response?.data?.content) {
        const newReplies = response.data.content as Comment[];
        // 트리에 추가 삽입
        setComments((prev) =>
          insertRepliesRecursively(prev, parentNum, newReplies)
        );
      }
    } catch (err) {
      console.error("대댓글 추가 로드 실패", err);
      setError("대댓글 추가 로드 중 오류가 발생했습니다.");
    }
  }, []);

  return {
    comments,
    fetchComments,
    addComment,
    replyToComment,
    likeComment,
    loadMoreReplies,
    error,
  };
}
/**
 * 댓글 배열을 받아서 트리 구조로 변환한다.
 */
function buildCommentTree(comments: Comment[]): Comment[] {
  // (1) 모든 댓글을 id -> comment 매핑으로 보관
  const map: Record<number, Comment> = {};

  // (2) 각 댓글의 replies 배열을 초기화 + map에 저장
  comments.forEach((c) => {
    c.replies = c.replies || [];
    map[c.id] = c;
  });

  // (3) 최상위 댓글(부모가 없는 것)들을 담을 배열
  const rootComments: Comment[] = [];

  comments.forEach((c) => {
    if (c.parentNum) {
      // 부모 댓글이 있는 경우
      const parent = map[c.parentNum];
      if (parent) {
        // 부모의 replies에 현재 댓글을 push
        parent.replies = parent.replies || [];
        parent.replies.push(c);
      }
    } else {
      // parentNum == null → 최상위 댓글
      rootComments.push(c);
    }
  });

  return rootComments;
}

/**
 * 재귀적으로 parentNum를 찾아 새 대댓글을 삽입
 *     → 여러 단계의 대댓글(트리)에서도 동작
 */
function insertReplyRecursively(
  commentList: Comment[],
  parentNum: number,
  newReply: Comment
): Comment[] {
  return commentList.map((item) => {
    // (1) 부모를 찾으면 replies에 삽입
    if (item.id === parentNum) {
      return {
        ...item,
        replies: [newReply, ...(item.replies || [])],
      };
    } else if (item.replies && item.replies.length > 0) {
      // (2) 자식이 있다면 자식들 중에서도 parentNum 탐색
      return {
        ...item,
        replies: insertReplyRecursively(item.replies, parentNum, newReply),
      };
    } else {
      // (3) 해당 아이템도 자식도 부모Id가 아닐 경우 그대로 반환
      return item;
    }
  });
}

function insertRepliesRecursively(
  tree: Comment[],
  parentNum: number,
  newReplies: Comment[]
): Comment[] {
  return tree.map((item) => {
    if (item.id === parentNum) {
      // 부모 찾음 → 대댓글 합쳐주기
      return {
        ...item,
        replies: [...(item.replies || []), ...newReplies],
      };
    } else if (item.replies && item.replies.length > 0) {
      // 자식 쪽에서 찾을 수도 있으므로 재귀
      return {
        ...item,
        replies: insertRepliesRecursively(item.replies, parentNum, newReplies),
      };
    } else {
      return item;
    }
  });
}
