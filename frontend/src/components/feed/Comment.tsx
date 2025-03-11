import React, { useState } from "react";
import { Comment as CommentType } from "./useComments";

interface CommentProps {
  comment: CommentType;
  onLike: (commentId: number) => void;
  onReply: (commentId: number, content: string) => void;
  onLoadMoreReplies?:(parentId: number) => void;
}

const Comment: React.FC<CommentProps> = ({ comment, onLike, onReply, onLoadMoreReplies, }) => {
  const [isReplying, setIsReplying] = useState(false);
  const [replyContent, setReplyContent] = useState("");

  // 자식 댓글(대댓글) 표시 여부
  const [showReplies, setShowReplies] = useState(false);

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
            {comment.likeCount && comment.likeCount > 0 ? (
              <>좋아요 {comment.likeCount}개</>
            ) : (
              <>좋아요</>
            )}
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
        <div className="ml-2 flex items-center">
        <button
          onClick={handleLike}
          className="ml-2 text-gray-500 dark:text-gray-400 
                     hover:text-gray-700 dark:hover:text-gray-200 
                     flex items-center"
        >
          {/* 좋아요 상태에 따른 하트 아이콘 */}
          <span className="text-lg mr-1">{comment.isLikedByMe ? "❤️" : "🤍"}</span>
        </button>
        </div>
      </div>

      {comment.replies && comment.replies.length > 0 && (
        <div className="mt-3 ml-4">
          {!showReplies ? (
            // 아직 자식 댓글이 안 펼쳐졌을 때
            <button
              onClick={() => setShowReplies(true)}
              className="text-sm text-blue-500 dark:text-blue-400 mt-2"
            >
              답글보기 ({comment.replies.length}개)
            </button>
          ) : (
            // 펼쳐진 상태라면 자식 댓글들을 렌더링 + "숨기기" 버튼
            <>
              {comment.replies.map((child) => (
                <Comment
                  key={child.id}
                  comment={child}
                  onLike={onLike}
                  onReply={onReply}
                  onLoadMoreReplies={onLoadMoreReplies}
                />
              ))}

              <button
                onClick={() => setShowReplies(false)}
                className="text-sm text-blue-500 dark:text-blue-400 mt-2"
              >
                답글 숨기기
              </button>
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default Comment;
