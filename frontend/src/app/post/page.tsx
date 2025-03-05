"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { X, ArrowLeft } from "lucide-react";
import client from "@/lib/backend/client";
import type { paths } from "@/lib/backend/apiV1/schema";

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
      // 이미지 업로드
      const uploadPromises = selectedFiles
        ? Array.from(selectedFiles).map(async (file) => {
            const formData = new FormData();
            formData.append("file", file);

            const { data, error } = await client.POST("/api-v1/upload", {
              body: formData,
            });

            if (error) throw error;
            return data?.url;
          })
        : [];

      const uploadedImageUrls = await Promise.all(uploadPromises);

      // 포스트 생성 요청
      const { data, error } = await client.POST("/api-v1/post", {
        params: {
          query: {
            request: {
              memberId: 1, // TODO: 실제 로그인한 사용자 ID로 대체
              content: postContent,
              images: uploadedImageUrls.filter((url) => url !== undefined),
            },
          },
        },
      });

      // 성공 처리
      if (data) {
        router.push("/");
      } else if (error) {
        setError(error.message || "포스트 생성에 실패했습니다.");
      }
    } catch (err: any) {
      setError("포스트 생성 중 오류가 발생했습니다.");
      console.error(err);
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
            <button
              onClick={handleGoBack}
              className="absolute top-4 left-4 text-gray-700 hover:text-gray-900 text-3xl"
            >
              <ArrowLeft size={40} />
            </button>

            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-center flex-grow">
                새 게시물 생성하기
              </h2>
              <button
                onClick={handleCreatePost}
                disabled={isLoading}
                className="text-blue-500 font-bold hover:underline"
              >
                {isLoading ? "업로드 중..." : "공유하기"}
              </button>
            </div>

            <hr className="border-t-2 border-gray-300 w-full mb-4" />

            {error && (
              <div
                className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
                role="alert"
              >
                <span className="block sm:inline">{error}</span>
              </div>
            )}

            {imagePreviews.length > 0 ? (
              <div className="relative w-full h-[500px] bg-gray-200 border-2 border-gray-300 rounded-md">
                <img
                  src={imagePreviews[currentIndex]}
                  alt={`Preview ${currentIndex}`}
                  className="w-full h-full object-contain"
                />
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
            ) : (
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

            <div className="flex flex-col mb-4">
              <textarea
                className="w-full p-2 border border-gray-300 rounded-md"
                style={{ height: "150px", marginBottom: "0" }}
                placeholder="내용을 작성해주세요"
                value={postContent}
                onChange={handleContentChange}
                disabled={isLoading}
              />
              <p className="text-right text-sm text-gray-500 mt-2">
                {postContent.length} / 2200
              </p>
            </div>
          </div>
        </div>
      )}

      <button
        onClick={handleRequestClose}
        className="absolute top-4 right-4 text-white hover:text-gray-700 text-3xl font-extrabold"
      >
        <X size={40} />
      </button>

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
