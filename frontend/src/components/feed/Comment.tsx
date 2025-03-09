import React, { useState } from "react";
import { Comment as CommentType } from "./useComments";

interface CommentProps {
  comment: CommentType;
  onLike: (commentId: number) => void;
  onReply: (commentId: number, content: string) => void;
}

const Comment: React.FC<CommentProps> = ({ comment, onLike, onReply }) => {
  const [isReplying, setIsReplying] = useState(false);
  const [replyContent, setReplyContent] = useState("");

  const DEFAULT_VISIBLE = 2;
  const [visibleCount, setVisibleCount] = useState<number>(DEFAULT_VISIBLE);

  // 댓글 좋아요 핸들러
  const handleLike = () => {
    onLike(comment.id);
  };

  // 답글 폼 표시/숨김 토글
  const toggleReplyForm = () => {
    setIsReplying(!isReplying);
  };

  // 답글 제출 핸들러
  const handleReplySubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!replyContent.trim()) return;

    onReply(comment.id, replyContent);
    setReplyContent("");
    setIsReplying(false);
  };

  const totalReplies = comment.replies?.length ?? 0;
  const visibleReplies = comment.replies?.slice(0, visibleCount) ?? [];

  return (
    <div className="p-4 border-b border-gray-300 dark:border-gray-700">
      <div className="flex">
        <div className="w-8 h-8 rounded-full bg-gray-400 dark:bg-gray-600 mr-3"></div>
        <div className="flex-1">
          <div className="flex justify-between mb-1">
            <span className="font-medium text-sm text-gray-900 dark:text-gray-100">
              {comment.username}
            </span>
            <span className="text-xs text-gray-500 dark:text-gray-400">
              {comment.time}
            </span>
          </div>
          <p className="text-sm text-gray-700 dark:text-gray-300 mb-2">
            {comment.content}
          </p>
          <div className="flex items-center space-x-4 text-xs text-gray-500 dark:text-gray-400">
            <button
              onClick={handleLike}
              className="hover:text-gray-700 dark:hover:text-gray-200 flex items-center"
            >
              <span>{comment.likeCount} 좋아요</span>
            </button>
            <button
              onClick={toggleReplyForm}
              className="hover:text-gray-700 dark:hover:text-gray-200"
            >
              답글
            </button>
          </div>

          {/* 답글 입력 폼 */}
          {isReplying && (
            <form
              onSubmit={handleReplySubmit}
              className="mt-3 flex items-center"
            >
              <input
                type="text"
                placeholder="답글을 입력하세요..."
                className="flex-grow bg-gray-100 dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-full px-3 py-1 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400"
                value={replyContent}
                onChange={(e) => setReplyContent(e.target.value)}
                autoFocus
              />
              <button
                type="submit"
                disabled={!replyContent.trim()}
                className={`ml-2 text-sm font-medium ${
                  replyContent.trim()
                    ? "text-blue-500 dark:text-blue-400"
                    : "text-blue-300 dark:text-blue-800"
                }`}
              >
                게시
              </button>
            </form>
          )}
        </div>

        {/* 좋아요 버튼 - 좋아요가 있는 경우만 표시 */}
        {comment.likeCount > 0 && (
          <button
            onClick={handleLike}
            className="ml-2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200"
          >
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

      {comment.replies && comment.replies.length > 0 && (
        <div className="mt-3 ml-4">
          {comment.replies.map((child) => (
            <Comment
              key={child.id}
              comment={child}
              onLike={onLike}
              onReply={onReply}
            />
          ))}
          {totalReplies > visibleCount && (
            <button
              onClick={() => setVisibleCount(totalReplies)}
              className="text-sm text-blue-500 dark:text-blue-400 mt-2"
            >
              답글보기({totalReplies - visibleCount}개)
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default Comment;
