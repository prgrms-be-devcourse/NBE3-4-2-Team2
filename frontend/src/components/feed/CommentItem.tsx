"use client";

import { useState } from "react";
import { components } from "../../lib/backend/apiV1/schema";

type CommentResponse = components["schemas"]["CommentResponse"];

interface CommentItemProps {
  comment: CommentResponse;
  isReply?: boolean;
  currentUserId: number;
  onReply: (commentId: number) => void;
  onRefresh: () => void;
}

// 날짜 포맷팅 함수
const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(
    2,
    "0"
  )}.${String(date.getDate()).padStart(2, "0")} ${String(
    date.getHours()
  ).padStart(2, "0")}:${String(date.getMinutes()).padStart(2, "0")}`;
};

const CommentItem: React.FC<CommentItemProps> = ({
  comment,
  isReply = false,
  currentUserId,
  onReply,
  onRefresh,
}) => {
  const [isEditing, setIsEditing] = useState<boolean>(false);
  const [editedContent, setEditedContent] = useState<string>(
    comment.content || ""
  );
  const [showOptions, setShowOptions] = useState<boolean>(false);

  // 댓글 작성자인지 확인
  const isAuthor = comment.id === currentUserId; // 실제로는 댓글 객체에서 작성자 ID를 비교

  // 댓글 수정 함수
  const handleEdit = async () => {
    if (!editedContent.trim()) return;

    try {
      const updateData = {
        commentId: comment.id,
        memberId: currentUserId,
        content: editedContent,
      };

      const response = await fetch(`/api-v1/comment/${comment.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(updateData),
      });

      if (!response.ok) {
        throw new Error("댓글 수정에 실패했습니다.");
      }

      setIsEditing(false);
      onRefresh(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 수정 에러:", error);
    }
  };

  // 댓글 삭제 함수
  const handleDelete = async () => {
    // 삭제 확인
    if (!window.confirm("댓글을 삭제하시겠습니까?")) return;

    try {
      const response = await fetch(
        `/api-v1/comment/${comment.id}?memberId=${currentUserId}`,
        {
          method: "DELETE",
        }
      );

      if (!response.ok) {
        throw new Error("댓글 삭제에 실패했습니다.");
      }

      onRefresh(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 삭제 에러:", error);
    }
  };

  // 옵션 메뉴 토글
  const toggleOptions = (e: React.MouseEvent) => {
    e.stopPropagation();
    setShowOptions(!showOptions);
  };

  // 외부 클릭 시 옵션 메뉴 닫기
  const handleOutsideClick = () => {
    if (showOptions) setShowOptions(false);
  };

  return (
    <div
      className={`flex items-start ${isReply ? "ml-6" : ""}`}
      onClick={handleOutsideClick}
    >
      <div
        className={`${
          isReply ? "w-6 h-6" : "w-7 h-7"
        } rounded-full bg-gray-200 mr-2 flex-shrink-0`}
      ></div>
      <div className="flex-grow">
        {isEditing ? (
          // 수정 모드
          <div className="bg-gray-50 rounded-lg p-2">
            <div className="flex items-center mb-2">
              <p className="text-sm font-medium">{comment.username}</p>
            </div>
            <textarea
              value={editedContent}
              onChange={(e) => setEditedContent(e.target.value)}
              className="w-full text-sm border rounded p-2 focus:outline-none focus:ring-1 focus:ring-blue-500"
              rows={2}
            />
            <div className="flex justify-end mt-2 space-x-2">
              <button
                type="button"
                onClick={() => setIsEditing(false)}
                className="px-3 py-1 text-xs bg-gray-200 rounded hover:bg-gray-300"
              >
                취소
              </button>
              <button
                type="button"
                onClick={handleEdit}
                className="px-3 py-1 text-xs bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                저장
              </button>
            </div>
          </div>
        ) : (
          // 보기 모드
          <div className="bg-gray-50 rounded-lg px-3 py-2 relative">
            <div className="flex items-center justify-between">
              <p className="text-sm font-medium">{comment.username}</p>

              {isAuthor && (
                <div className="relative">
                  <button
                    onClick={toggleOptions}
                    className="text-gray-500 text-sm"
                  >
                    ···
                  </button>

                  {showOptions && (
                    <div className="absolute right-0 mt-1 w-20 bg-white rounded shadow-lg z-10">
                      <button
                        onClick={() => {
                          setIsEditing(true);
                          setShowOptions(false);
                        }}
                        className="block w-full text-left px-4 py-2 text-xs hover:bg-gray-100"
                      >
                        수정
                      </button>
                      <button
                        onClick={handleDelete}
                        className="block w-full text-left px-4 py-2 text-xs text-red-500 hover:bg-gray-100"
                      >
                        삭제
                      </button>
                    </div>
                  )}
                </div>
              )}
            </div>
            <p className="text-sm break-words">{comment.content}</p>
          </div>
        )}

        <div className="flex items-center mt-1 text-xs text-gray-500 space-x-3">
          <span>{comment.createdAt ? formatDate(comment.createdAt) : ""}</span>
          <button
            type="button"
            onClick={() => onReply(comment.id || 0)}
            className="hover:text-gray-700"
          >
            답글 달기
          </button>
        </div>
      </div>
    </div>
  );
};

export default CommentItem;
