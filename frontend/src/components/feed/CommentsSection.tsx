import React from "react";
import { Comment as CommentType } from "./dummyComments";
import Comment from "./Comment";

interface CommentsSectionProps {
  comments: CommentType[];
  onAddComment: (content: string) => void;
  onLikeComment: (commentId: number) => void;
  onReplyComment: (commentId: number, content: string) => void;
}

const CommentsSection: React.FC<CommentsSectionProps> = ({
  comments,
  onAddComment,
  onLikeComment,
  onReplyComment,
}) => {
  const [newComment, setNewComment] = React.useState("");

  // 새 댓글 제출 핸들러
  const handleSubmitComment = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    onAddComment(newComment);
    setNewComment("");
  };

  return (
    <div className="w-full md:w-[420px] border-l border-gray-300 dark:border-gray-700 flex flex-col">
      {/* 댓글 헤더 */}
      <div className="p-4 border-b border-gray-300 dark:border-gray-700">
        <h2 className="text-lg font-medium text-gray-900 dark:text-gray-100">댓글</h2>
      </div>

      {/* 댓글 목록 스크롤 영역 */}
      <div className="flex-1 overflow-y-auto">
        {comments.length > 0 ? (
          comments.map((comment) => (
            <Comment
              key={comment.id}
              comment={comment}
              onLike={onLikeComment}
              onReply={onReplyComment}
            />
          ))
        ) : (
          <div className="p-4 text-center text-gray-500 dark:text-gray-400">
            아직 댓글이 없습니다. 첫 댓글을 작성해보세요!
          </div>
        )}
      </div>

      {/* 새 댓글 입력 */}
      <div className="p-4 border-t border-gray-300 dark:border-gray-700">
        <form onSubmit={handleSubmitComment} className="flex items-center">
          <input
            type="text"
            placeholder="새 댓글을 입력하세요..."
            className="flex-grow bg-transparent border-0 focus:ring-0 text-sm text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
          />
          <button
            type="submit"
            disabled={!newComment.trim()}
            className={`text-sm font-medium ${
              newComment.trim() ? "text-blue-500 dark:text-blue-400" : "text-blue-300 dark:text-blue-800"
            }`}
          >
            게시
          </button>
        </form>
      </div>
    </div>
  );
};

export default CommentsSection;
