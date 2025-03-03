"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { X } from "lucide-react"; // lucide-react에서 X 아이콘을 가져옵니다.

export default function PostCreatePage() {
  const [isModalOpen, setIsModalOpen] = useState(true); // 기본값을 true로 설정해서 페이지 로드 시 모달이 열리도록 함
  const [postContent, setPostContent] = useState(""); // 게시물 내용 상태
  const router = useRouter(); // useRouter 훅을 사용하여 페이지 이동

  // 모달 닫기 버튼 클릭 시 모달 닫기
  const handleCloseModal = () => {
    setIsModalOpen(false);
    router.push("/"); // 모달이 닫히면 / 로 리디렉션
  };

  // 텍스트 변경 시 글자수 업데이트
  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setPostContent(e.target.value); // 입력값 상태 업데이트
  };
  
  return (
    <div>
      {isModalOpen && (
        <div className="modal-overlay fixed inset-0 bg-gray-800 bg-opacity-50 flex justify-center items-center">
          <div
            className="modal-content bg-white p-6 rounded-xl shadow-xl"
            style={{
              width: "1000px",
              height: "800px",
              maxHeight: "80vh", // 모달 높이를 화면에 맞게 제한
              overflowY: "auto", // 콘텐츠가 많을 경우 스크롤 가능
            }}
          >
            <h2 className="text-xl font-bold mb-4 text-center">새 게시물 생성하기</h2>

            {/* 게시물 내용 작성 */}
            <input 
              type="file" 
              accept="image/*" 
              multiple
              className="block w-full mb-4 p-2 border border-gray-300 rounded-md"
              style={{ width: "950px", height: "500px" }} // 크기 수정
            />
            <textarea
              className="w-full h-32 p-2 border border-gray-300 rounded-md mb-4"
              placeholder="내용을 작성해주세요"
              value={postContent}
              onChange={handleContentChange} // 글자수 업데이트
            />

            <p className="text-right text-sm text-gray-500">{postContent.length}자</p>

            {/* 닫기 버튼 (아이콘 사용) */}
            <button
              onClick={handleCloseModal} // 모달 닫기
              className="absolute top-4 right-4 text-white hover:text-gray-700 text-3xl font-extrabold"
            >
              <X size={40} /> {/* X 아이콘 크기를 40  px로 설정 */}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
