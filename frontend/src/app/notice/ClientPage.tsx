"use client";

import React, { useEffect, useState } from "react";
import { Bell, User, MessageSquare, Heart, Clock } from "lucide-react";
import { format } from "date-fns";
import { ko } from "date-fns/locale";

// 알림 타입 정의
type NotificationType = "COMMENT" | "LIKE" | "FOLLOW";

// API 응답 타입 정의
interface NotificationResponse {
  notificationId: number;
  type: NotificationType;
  targetId: number;
  message: string;
  isRead: boolean;
  createdAt: string;
}

interface NotificationPageResponse {
  responses: NotificationResponse[];
  totalCount: number;
  currentPage: number;
  totalPageCount: number;
}

interface RsData<T> {
  time: string;
  isSuccess: boolean;
  message: string | null;
  data: T;
}

const ClientPage = () => {
  const [notifications, setNotifications] = useState<NotificationResponse[]>(
    []
  );
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [totalCount, setTotalCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // API 호출 함수
  const fetchNotifications = async (page: number) => {
    try {
      setLoading(true);

      // 토큰 가져오기
      const token =
        localStorage.getItem("token") || sessionStorage.getItem("token");

      // 백엔드 서버 URL 설정 (8080 포트)
      const baseUrl = "http://localhost:8080";
      const apiUrl = `${baseUrl}/api-v1/notification/list?page=${page}`;
      console.log("API 요청 URL:", apiUrl);

      const response = await fetch(apiUrl, {
        headers: {
          Authorization: token ? `Bearer ${token}` : "",
          "Content-Type": "application/json",
        },
        credentials: "include", // 쿠키 기반 인증을 사용하는 경우
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error("API 응답 오류:", response.status, errorText);
        throw new Error(
          `알림 목록을 불러오는데 실패했습니다. 상태 코드: ${response.status}`
        );
      }

      const data: RsData<NotificationPageResponse> = await response.json();

      if (data.isSuccess) {
        setNotifications(data.data.responses);
        setCurrentPage(data.data.currentPage);
        setTotalPages(data.data.totalPageCount);
        setTotalCount(data.data.totalCount);
      } else {
        console.error("API 응답 실패:", data);
        throw new Error(data.message || "알림 목록을 불러오는데 실패했습니다.");
      }
    } catch (err) {
      console.error("API 호출 오류:", err);
      setError(
        err instanceof Error
          ? err.message
          : "알림 목록을 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications(currentPage);
  }, [currentPage]);

  // 알림 타입에 따른 아이콘 반환
  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case "COMMENT":
        return <MessageSquare className="text-blue-500" size={20} />;
      case "LIKE":
        return <Heart className="text-red-500" size={20} />;
      case "FOLLOW":
        return <User className="text-green-500" size={20} />;
      default:
        return <Bell className="text-gray-500" size={20} />;
    }
  };

  // 알림 타입 한글 이름 반환
  const getNotificationTypeName = (type: NotificationType) => {
    switch (type) {
      case "COMMENT":
        return "댓글";
      case "LIKE":
        return "좋아요";
      case "FOLLOW":
        return "팔로우";
      default:
        return "알림";
    }
  };

  // 날짜 포맷팅
  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return format(date, "yyyy년 MM월 dd일 HH:mm", { locale: ko });
    } catch (e) {
      return dateString;
    }
  };

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    if (page >= 0 && page < totalPages) {
      setCurrentPage(page);
    }
  };

  // 페이지네이션 버튼 생성
  const renderPagination = () => {
    const pages = [];
    const maxButtons = 5;

    // 시작 페이지와 끝 페이지 계산
    let startPage = Math.max(0, currentPage - Math.floor(maxButtons / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxButtons - 1);

    // 최대 버튼 수에 맞게 조정
    if (endPage - startPage + 1 < maxButtons) {
      startPage = Math.max(0, endPage - maxButtons + 1);
    }

    // 이전 페이지 버튼
    if (currentPage > 0) {
      pages.push(
        <button
          key="prev"
          onClick={() => handlePageChange(currentPage - 1)}
          className="px-3 py-1 rounded-md bg-gray-200 hover:bg-gray-300"
        >
          이전
        </button>
      );
    }

    // 페이지 번호 버튼
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          onClick={() => handlePageChange(i)}
          className={`px-3 py-1 rounded-md ${
            i === currentPage
              ? "bg-blue-500 text-white"
              : "bg-gray-200 hover:bg-gray-300"
          }`}
        >
          {i + 1}
        </button>
      );
    }

    // 다음 페이지 버튼
    if (currentPage < totalPages - 1) {
      pages.push(
        <button
          key="next"
          onClick={() => handlePageChange(currentPage + 1)}
          className="px-3 py-1 rounded-md bg-gray-200 hover:bg-gray-300"
        >
          다음
        </button>
      );
    }

    return pages;
  };

  if (loading && notifications.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 p-4 rounded-md text-red-700">
        <p>{error}</p>
        <button
          onClick={() => fetchNotifications(currentPage)}
          className="mt-2 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="p-4 border-b flex justify-between items-center">
        <div>
          <span className="font-medium">전체 알림</span>
          <span className="ml-2 bg-blue-100 text-blue-800 text-xs font-medium px-2.5 py-0.5 rounded">
            {totalCount}
          </span>
        </div>
        <div className="text-sm text-gray-500">
          페이지 {currentPage + 1} / {totalPages}
        </div>
      </div>

      {notifications.length > 0 ? (
        <ul className="divide-y divide-gray-200">
          {notifications.map((notification) => (
            <li
              key={notification.notificationId}
              className={`p-4 hover:bg-gray-50 transition-colors ${
                notification.isRead ? "bg-gray-50" : ""
              }`}
            >
              <div className="flex items-start">
                <div className="flex-shrink-0 mt-1">
                  {getNotificationIcon(notification.type)}
                </div>
                <div className="ml-3 flex-1">
                  <div className="flex items-center">
                    <span
                      className={`text-xs font-medium px-2 py-0.5 rounded mr-2 ${
                        notification.isRead
                          ? "bg-gray-200 text-gray-800"
                          : "bg-blue-100 text-blue-800"
                      }`}
                    >
                      {getNotificationTypeName(notification.type)}
                    </span>
                    {!notification.isRead && (
                      <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-0.5 rounded">
                        새 알림
                      </span>
                    )}
                  </div>
                  <p
                    className={`mt-1 text-sm ${
                      notification.isRead
                        ? "text-gray-500"
                        : "text-gray-900 font-medium"
                    }`}
                  >
                    {notification.message}
                  </p>
                  <div className="mt-2 text-xs text-gray-500">
                    {formatDate(notification.createdAt)}
                  </div>
                </div>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <div className="p-8 text-center text-gray-500">알림이 없습니다.</div>
      )}

      {totalPages > 1 && (
        <div className="p-4 border-t flex justify-center gap-2">
          {renderPagination()}
        </div>
      )}
    </div>
  );
};

export default ClientPage;
