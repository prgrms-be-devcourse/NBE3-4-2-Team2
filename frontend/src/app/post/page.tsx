"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { X, ArrowLeft } from "lucide-react";
import client from "@/lib/backend/client";
import { getCurrentUserId } from "../../utils/jwtUtils";

export default function PostCreatePage() {
  const [isModalOpen, setIsModalOpen] = useState(true);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [postContent, setPostContent] = useState("");
  const [selectedFiles, setSelectedFiles] = useState<FileList | null>(null);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const handleRequestClose = () => setIsConfirmModalOpen(true);
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setIsConfirmModalOpen(false);
    router.push("/");
  };
  const handleCancelClose = () => setIsConfirmModalOpen(false);

  const handleGoBack = () => {
    if (selectedFiles?.length) {
      setSelectedFiles(null);
      setImagePreviews([]);
    } else {
      handleRequestClose();
    }
  };

  const handleCreatePost = async () => {
    // 입력 유효성 검사
    if (!postContent.trim() && (!selectedFiles || selectedFiles.length === 0)) {
      setError("게시물 내용이나 이미지를 추가해주세요.");
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      // JWT에서 사용자 ID 가져오기
      const userId = getCurrentUserId();

      if (!userId) {
        setError("로그인이 필요합니다.");
        setIsLoading(false);
        return;
      }

      // FormData 생성 및 필드 추가
      const formData = new FormData();
      formData.append("memberId", userId.toString()); // JWT에서 가져온 ID 사용
      formData.append("content", postContent);

      if (selectedFiles) {
        for (let i = 0; i < selectedFiles.length; i++) {
          formData.append("images", selectedFiles[i]);
        }
      }

      const result = await client.POST("/api-v1/post", {
        body: formData,
      });

      console.log("업로드 성공:", result);
      router.push("/");
    } catch (err) {
      console.error("업로드 실패:", err);
      setError("포스트 생성 중 오류가 발생했습니다.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const { value } = e.target;
    if (value.length <= 2200) setPostContent(value);
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length <= 10) {
      setSelectedFiles(files);

      const previews: string[] = [];
      Array.from(files).forEach((file) => {
        const reader = new FileReader();
        reader.onloadend = () => {
          if (reader.result) previews.push(reader.result as string);
          if (previews.length === files.length) setImagePreviews(previews);
        };
        reader.readAsDataURL(file);
      });
    } else {
      alert("이미지는 최대 10개까지만 선택할 수 있습니다.");
    }
  };

  useEffect(() => {
    if (imagePreviews.length === 0) setCurrentIndex(0);
  }, [imagePreviews]);

  // 모달이 열렸을 때 body에 overflow: hidden 추가
  useEffect(() => {
    if (isModalOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }

    return () => {
      document.body.style.overflow = "";
    };
  }, [isModalOpen]);

  return (
    <div className="fixed inset-0 z-50">
      {isModalOpen && (
        <div
          className="modal-overlay fixed inset-0 bg-black bg-opacity-75 dark:bg-opacity-85 flex justify-center items-center z-50"
          onClick={handleRequestClose}
        >
          <div
            className="modal-content bg-white dark:bg-gray-800 p-6 rounded-xl shadow-xl relative max-h-[90vh] w-[1000px] max-w-[95vw]"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              onClick={handleGoBack}
              className="absolute top-4 left-4 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white text-3xl"
            >
              <ArrowLeft size={40} />
            </button>

            <div className="relative flex items-center justify-center mb-4 px-12">
              <h2 className="text-xl font-bold text-center flex-grow dark:text-white">
                새 게시물 생성하기
              </h2>
              <button
                onClick={handleCreatePost}
                disabled={isLoading}
                className="absolute right-12 text-blue-500 dark:text-blue-400 font-bold hover:underline"
              >
                {isLoading ? "업로드 중..." : "공유하기"}
              </button>
            </div>

            <hr className="border-t-2 border-gray-300 dark:border-gray-700 w-full mb-4" />

            <div
              className="overflow-y-auto"
              style={{ maxHeight: "calc(90vh - 180px)" }}
            >
              {error && (
                <div
                  className="bg-red-100 dark:bg-red-900/50 border border-red-400 dark:border-red-800 text-red-700 dark:text-red-300 px-4 py-3 rounded relative mb-4"
                  role="alert"
                >
                  <span className="block sm:inline">{error}</span>
                </div>
              )}

              {imagePreviews.length > 0 ? (
                <div className="relative w-full h-[500px] bg-gray-200 dark:bg-gray-700 border-2 border-gray-300 dark:border-gray-600 rounded-md">
                  <img
                    src={imagePreviews[currentIndex]}
                    alt={`Preview ${currentIndex}`}
                    className="w-full h-full object-contain"
                  />
                  <button
                    onClick={() =>
                      setCurrentIndex((prev) => Math.max(prev - 1, 0))
                    }
                    className={`absolute left-4 top-1/2 transform -translate-y-1/2 text-white bg-black bg-opacity-50 dark:bg-opacity-70 p-2 rounded-full ${
                      currentIndex === 0 ? "opacity-50 cursor-not-allowed" : ""
                    }`}
                    disabled={currentIndex === 0}
                  >
                    &lt;
                  </button>
                  <button
                    onClick={() =>
                      setCurrentIndex((prev) =>
                        Math.min(prev + 1, imagePreviews.length - 1)
                      )
                    }
                    className={`absolute right-4 top-1/2 transform -translate-y-1/2 text-white bg-black bg-opacity-50 dark:bg-opacity-70 p-2 rounded-full ${
                      currentIndex === imagePreviews.length - 1
                        ? "opacity-50 cursor-not-allowed"
                        : ""
                    }`}
                    disabled={currentIndex === imagePreviews.length - 1}
                  >
                    &gt;
                  </button>
                  <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex space-x-1">
                    {imagePreviews.map((_, index) => (
                      <div key={index} className="flex flex-col items-center">
                        <div
                          className={`w-2 h-2 rounded-full ${
                            index === currentIndex
                              ? "bg-white"
                              : "bg-gray-500 dark:bg-gray-400"
                          }`}
                        />
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <div
                  className="w-full h-[500px] bg-gray-200 dark:bg-gray-700 border-2 border-gray-300 dark:border-gray-600 rounded-md flex justify-center items-center cursor-pointer relative"
                  onClick={() => document.getElementById("fileInput")?.click()}
                >
                  <p className="text-gray-500 dark:text-gray-300 text-4xl font-bold">
                    +
                  </p>
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

              <div className="flex flex-col mb-4 mt-4">
                <textarea
                  className="w-full p-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-black dark:text-white"
                  style={{ height: "150px", marginBottom: "0" }}
                  placeholder="내용을 작성해주세요"
                  value={postContent}
                  onChange={handleContentChange}
                  disabled={isLoading}
                />
                <p className="text-right text-sm text-gray-500 dark:text-gray-400 mt-2">
                  {postContent.length} / 2200
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {isConfirmModalOpen && (
        <div className="fixed inset-0 flex justify-center items-center bg-black bg-opacity-75 dark:bg-opacity-85 z-[60]">
          <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-lg text-center">
            <p className="mb-4 text-lg font-bold dark:text-white">
              정말로 나가시겠습니까?
            </p>
            <div className="flex justify-center gap-4">
              <button
                onClick={handleCloseModal}
                className="px-4 py-2 bg-red-500 dark:bg-red-600 text-white rounded-md hover:bg-red-600 dark:hover:bg-red-700"
              >
                나가기
              </button>
              <button
                onClick={handleCancelClose}
                className="px-4 py-2 bg-gray-300 dark:bg-gray-600 text-black dark:text-white rounded-md hover:bg-gray-400 dark:hover:bg-gray-700"
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
