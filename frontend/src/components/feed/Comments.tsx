"use client";

import { useState, useEffect } from "react";
import { components } from "../../lib/backend/apiV1/schema";
import CommentItem from "./CommentItem";

type CommentResponse = components["schemas"]["CommentResponse"];
type PageCommentResponse = components["schemas"]["PageCommentResponse"];

interface CommentsProps {
  postId: number;
}

const Comments: React.FC<CommentsProps> = ({ postId }) => {
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [newComment, setNewComment] = useState<string>("");
  const [replyTo, setReplyTo] = useState<number | null>(null);
  const [expandedReplies, setExpandedReplies] = useState<
    Record<number, boolean>
  >({});
  const [replies, setReplies] = useState<Record<number, CommentResponse[]>>({});

  // 현재 로그인한 사용자의 ID (실제로는 세션에서 가져와야 함)
  const currentUserId = 1; // 임시 데이터

  // 댓글 목록을 불러오는 함수
  const fetchComments = async (page: number = 0) => {
    try {
      setLoading(true);
      // API 호출
      const response = await fetch(
        `/api-v1/comment/post/${postId}?page=${page}&size=10`
      );

      if (!response.ok) {
        throw new Error("댓글을 불러오는데 실패했습니다.");
      }

      const data = await response.json();
      const commentData = data.data as PageCommentResponse;

      setComments(commentData.content || []);
      setTotalPages(commentData.totalPages || 0);
      setCurrentPage(page);
    } catch (error) {
      console.error("댓글 로딩 에러:", error);
      // 실제 구현시에는 에러 상태를 관리할 수 있음
    } finally {
      setLoading(false);
    }
  };

  // 답글 불러오기 함수
  const fetchReplies = async (parentId: number) => {
    try {
      const response = await fetch(
        `/api-v1/comment/replies/${parentId}?page=0&size=10`
      );

      if (!response.ok) {
        throw new Error("답글을 불러오는데 실패했습니다.");
      }

      const data = await response.json();
      const replyData = data.data as PageCommentResponse;

      setReplies((prev) => ({
        ...prev,
        [parentId]: replyData.content || [],
      }));
    } catch (error) {
      console.error("답글 로딩 에러:", error);
    }
  };

  // 댓글 작성 함수
  const handleSubmitComment = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!newComment.trim()) return;

    try {
      const commentData = {
        postId: postId,
        memberId: currentUserId,
        content: newComment,
        parentId: replyTo,
      };

      const response = await fetch("/api-v1/comment", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(commentData),
      });

      if (!response.ok) {
        throw new Error("댓글 작성에 실패했습니다.");
      }

      setNewComment("");
      setReplyTo(null);

      // 답글을 작성한 경우
      if (replyTo) {
        fetchReplies(replyTo);
        setExpandedReplies((prev) => ({ ...prev, [replyTo]: true }));
      } else {
        // 새 댓글을 작성한 경우, 댓글 목록 새로고침
        fetchComments(currentPage);
      }
    } catch (error) {
      console.error("댓글 작성 에러:", error);
    }
  };

  // 답글 토글 함수
  const toggleReplies = (commentId: number) => {
    if (!expandedReplies[commentId]) {
      fetchReplies(commentId);
    }

    setExpandedReplies((prev) => ({
      ...prev,
      [commentId]: !prev[commentId],
    }));
  };

  // 답글 작성 모드 설정
  const handleReplyClick = (commentId: number | null) => {
    setReplyTo(commentId);
    setNewComment("");
  };

  // 초기 로딩
  useEffect(() => {
    fetchComments();
  }, [postId]);

  return (
    <div className="px-3 py-2">
      <h3 className="text-sm font-medium mb-3">댓글</h3>

      {/* 댓글 입력 폼 */}
      <form onSubmit={handleSubmitComment} className="mb-4">
        <div className="flex items-start">
          <div className="w-7 h-7 rounded-full bg-gray-200 mr-2 flex-shrink-0"></div>
          <div className="flex-grow relative">
            {replyTo !== null && (
              <div className="bg-gray-100 rounded px-2 py-1 text-xs mb-1 flex items-center">
                <span>답글 작성 중</span>
                <button
                  type="button"
                  onClick={() => handleReplyClick(null)}
                  className="ml-2 text-gray-500"
                >
                  ✕
                </button>
              </div>
            )}
            <input
              type="text"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder={
                replyTo ? "답글을 입력하세요..." : "댓글을 입력하세요..."
              }
              className="w-full border rounded-full py-1 px-3 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <button
              type="submit"
              disabled={!newComment.trim()}
              className={`absolute right-3 top-1/2 transform -translate-y-1/2 text-sm ${
                newComment.trim() ? "text-blue-500" : "text-gray-400"
              }`}
            >
              게시
            </button>
          </div>
        </div>
      </form>

      {/* 댓글 목록 */}
      {loading ? (
        <div className="text-center py-4">
          <p className="text-sm text-gray-500">댓글을 불러오는 중...</p>
        </div>
      ) : comments.length === 0 ? (
        <div className="text-center py-4">
          <p className="text-sm text-gray-500">
            아직 댓글이 없습니다. 첫 댓글을 남겨보세요!
          </p>
        </div>
      ) : (
        <div className="space-y-3">
          {comments.map((comment) => (
            <div key={comment.id} className="comment-item">
              {/* 댓글 내용 */}
              <CommentItem
                comment={comment}
                currentUserId={currentUserId}
                onReply={handleReplyClick}
                onRefresh={() => fetchComments(currentPage)}
              />

              {/* 답글 영역 */}
              {expandedReplies[comment.id || 0] && replies[comment.id || 0] && (
                <div className="ml-9 mt-2 space-y-2">
                  {replies[comment.id || 0].map((reply) => (
                    <CommentItem
                      key={reply.id}
                      comment={reply}
                      isReply={true}
                      currentUserId={currentUserId}
                      onReply={() => handleReplyClick(comment.id || 0)}
                      onRefresh={() => fetchReplies(comment.id || 0)}
                    />
                  ))}
                </div>
              )}

              {/* 답글 토글 버튼 */}
              {comment.ref && comment.ref > 0 && (
                <div className="ml-9 mt-1">
                  <button
                    type="button"
                    onClick={() => toggleReplies(comment.id || 0)}
                    className="text-xs text-gray-500 hover:text-gray-700 flex items-center"
                  >
                    <span>
                      {expandedReplies[comment.id || 0]
                        ? "답글 숨기기"
                        : "답글 보기"}
                    </span>
                    <span className="ml-1">
                      {expandedReplies[comment.id || 0] ? "▲" : "▼"}
                    </span>
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-4">
          <div className="inline-flex">
            <button
              onClick={() => fetchComments(currentPage - 1)}
              disabled={currentPage === 0}
              className={`px-3 py-1 text-sm ${
                currentPage === 0
                  ? "text-gray-400 cursor-not-allowed"
                  : "text-blue-500 hover:bg-blue-50"
              } rounded`}
            >
              이전
            </button>
            <span className="px-3 py-1 text-sm text-gray-700">
              {currentPage + 1} / {totalPages}
            </span>
            <button
              onClick={() => fetchComments(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
              className={`px-3 py-1 text-sm ${
                currentPage === totalPages - 1
                  ? "text-gray-400 cursor-not-allowed"
                  : "text-blue-500 hover:bg-blue-50"
              } rounded`}
            >
              다음
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Comments;
