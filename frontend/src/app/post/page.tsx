"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { X, ArrowLeft } from "lucide-react";

export default function PostCreatePage() {
  const [isModalOpen, setIsModalOpen] = useState(true);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [postContent, setPostContent] = useState("");
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const router = useRouter();

  // 모달 닫기 요청 시 확인 모달 열기
  const handleRequestClose = () => {
    setIsConfirmModalOpen(true);
  };

  // 확인 모달에서 "나가기" 선택 시 닫기
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setIsConfirmModalOpen(false);
    router.push("/"); // 홈으로 리디렉션
  };

  // 확인 모달에서 "취소" 선택 시 유지
  const handleCancelClose = () => {
    setIsConfirmModalOpen(false);
  };

  // 뒤로가기 버튼 클릭 시 동작
  const handleGoBack = () => {
    if (selectedFiles && selectedFiles.length > 0) {
      // 이미지를 선택한 후에는 선택 화면으로 돌아가도록 처리
      setSelectedFiles(null); // 이미지를 선택한 상태를 초기화
      setImagePreviews([]); // 미리보기 이미지 초기화
    } else {
      // 이미지를 선택하지 않았다면 확인 모달을 띄워야 함
      handleRequestClose(); // 확인 모달 띄우기
    }
  };

  // 게시물 생성 로직 (임시 핸들러)
  const handleCreatePost = () => {
    console.log("게시물 생성!");
    // 여기에 게시물 업로드 로직 추가 예정
  };

  // 텍스트 변경 시 글자수 업데이트
  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    if (e.target.value.length <= 2200) {
      setPostContent(e.target.value);
    }
  };

  // 파일 선택 핸들러
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      if (e.target.files.length <= 10) {
        setSelectedFiles(e.target.files);

        const previews: string[] = [];
        for (let i = 0; i < e.target.files.length; i++) {
          const file = e.target.files[i];
          const reader = new FileReader();
          reader.onloadend = () => {
            if (reader.result) {
              previews.push(reader.result as string);
              if (previews.length === e.target.files?.length) {
                setImagePreviews(previews);
              }
            }
          };
          reader.readAsDataURL(file);
        }
      } else {
        alert("이미지는 최대 10개까지만 선택할 수 있습니다.");
      }
    }
  };

  useEffect(() => {
    if (imagePreviews.length === 0) {
      setCurrentIndex(0);
    }
  }, [imagePreviews]);

  return (
    <div>
      {isModalOpen && (
        <div
          className="modal-overlay fixed inset-0 bg-gray-800 bg-opacity-50 flex justify-center items-center"
          onClick={handleRequestClose}
        >
          <div
            className="modal-content bg-white p-6 rounded-xl shadow-xl relative"
            style={{
              width: "1000px",
              height: "800px",
              maxHeight: "80vh",
              overflowY: "auto",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* 좌측 뒤로가기 버튼 */}
            <button
              onClick={handleGoBack} // 뒤로가기 버튼 클릭 시 동작
              className="absolute top-4 left-4 text-gray-700 hover:text-gray-900 text-3xl"
            >
              <ArrowLeft size={40} />
            </button>

            {/* 게시물 생성 */}
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-center flex-grow">
                새 게시물 생성하기
              </h2>
              <button
                onClick={handleCreatePost}
                className="text-blue-500 font-bold hover:underline"
              >
                공유하기
              </button>
            </div>

            <hr className="border-t-2 border-gray-300 w-full mb-4" />
            {/* 이미지 슬라이더 */}
            {imagePreviews.length > 0 && (
              <div className="relative w-full h-[500px] bg-gray-200 border-2 border-gray-300 rounded-md">
                <img
                  src={imagePreviews[currentIndex]}
                  alt={`Preview ${currentIndex}`}
                  className="w-full h-full object-contain"
                />
                {/* 이전 버튼 */}
                <button
                  onClick={() =>
                    setCurrentIndex((prev) => Math.max(prev - 1, 0))
                  }
                  className={`absolute left-4 top-1/2 transform -translate-y-1/2 text-white bg-black bg-opacity-50 p-2 rounded-full ${
                    currentIndex === 0 ? "opacity-50 cursor-not-allowed" : ""
                  }`}
                  disabled={currentIndex === 0}
                >
                  &lt;
                </button>
                {/* 다음 버튼 */}
                <button
                  onClick={() =>
                    setCurrentIndex((prev) =>
                      Math.min(prev + 1, imagePreviews.length - 1)
                    )
                  }
                  className={`absolute right-4 top-1/2 transform -translate-y-1/2 text-white bg-black bg-opacity-50 p-2 rounded-full ${
                    currentIndex === imagePreviews.length - 1
                      ? "opacity-50 cursor-not-allowed"
                      : ""
                  }`}
                  disabled={currentIndex === imagePreviews.length - 1}
                >
                  &gt;
                </button>
                {/* 이미지 번호 도트 표시 */}
                <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-1">
                  {imagePreviews.map((_, index) => (
                    <div key={index} className="flex flex-col items-center">
                      <div
                        className={`w-2 h-2 rounded-full ${
                          index === currentIndex ? "bg-white" : "bg-gray-500"
                        }`}
                      />
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* 파일 선택 버튼 */}
            {!selectedFiles && (
              <div
                className="w-full h-[500px] bg-gray-200 border-2 border-gray-300 rounded-md flex justify-center items-center cursor-pointer relative"
                onClick={() => document.getElementById("fileInput")?.click()}
              >
                <p className="text-gray-500 text-4xl font-bold">+</p>
                <input
                  type="file"
                  accept="image/*"
                  multiple
                  id="fileInput"
                  className="hidden"
                  onChange={handleFileChange}
                />
              </div>
            )}

            {/* 게시물 작성란 */}
            <div className="flex flex-col mb-4">
              <textarea
                className="w-full p-2 border border-gray-300 rounded-md"
                style={{ height: "150px", marginBottom: "0" }}
                placeholder="내용을 작성해주세요"
                value={postContent}
                onChange={handleContentChange}
              />
              <p
                className="text-right text-sm text-gray-500 mt-2"
                style={{ marginTop: "0px", marginBottom: "0px" }}
              >
                {postContent.length} / 2200
              </p>
            </div>
          </div>
        </div>
      )}
      {/* 닫기 버튼 */}
      <button
        onClick={handleRequestClose}
        className="absolute top-4 right-4 text-white hover:text-gray-700 text-3xl font-extrabold"
      >
        <X size={40} />
      </button>

      {/* 확인 모달 */}
      {isConfirmModalOpen && (
        <div className="fixed inset-0 flex justify-center items-center bg-black bg-opacity-50">
          <div className="bg-white p-6 rounded-lg shadow-lg text-center">
            <p className="mb-4 text-lg font-bold">정말로 나가시겠습니까?</p>
            <div className="flex justify-center gap-4">
              <button
                onClick={handleCloseModal}
                className="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
              >
                나가기
              </button>
              <button
                onClick={handleCancelClose}
                className="px-4 py-2 bg-gray-300 rounded-md hover:bg-gray-400"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
