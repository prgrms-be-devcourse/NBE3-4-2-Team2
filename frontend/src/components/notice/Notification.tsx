"use client";

import React, { useState, useEffect } from "react";
import { X, MessageSquare, Heart, User, Bell, Clock } from "lucide-react";
import { format } from "date-fns";
import { ko } from "date-fns/locale";
import { NotificationType } from "@/hooks/useNotificationSSE";

interface ToastNotificationProps {
  id: number;
  type: NotificationType;
  message: string;
  createdAt: string;
  onClose: (id: number) => void;
  duration?: number;
}

export const ToastNotification: React.FC<ToastNotificationProps> = ({
  id,
  type,
  message,
  createdAt,
  onClose,
  duration = 5000, // 기본 5초 후 자동 닫힘
}) => {
  const [isVisible, setIsVisible] = useState(true);

  // 타입에 따른 아이콘과 색상 설정
  const getIconAndColor = (type: NotificationType) => {
    switch (type) {
      case "COMMENT":
        return {
          icon: <MessageSquare size={18} />,
          bgColor: "bg-blue-100",
          textColor: "text-blue-800",
          iconColor: "text-blue-500",
        };
      case "LIKE":
        return {
          icon: <Heart size={18} />,
          bgColor: "bg-red-100",
          textColor: "text-red-800",
          iconColor: "text-red-500",
        };
      case "FOLLOW":
        return {
          icon: <User size={18} />,
          bgColor: "bg-green-100",
          textColor: "text-green-800",
          iconColor: "text-green-500",
        };
      default:
        return {
          icon: <Bell size={18} />,
          bgColor: "bg-gray-100",
          textColor: "text-gray-800",
          iconColor: "text-gray-500",
        };
    }
  };

  const { icon, bgColor, textColor, iconColor } = getIconAndColor(type);

  // 날짜 포맷팅
  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return format(date, "HH:mm", { locale: ko });
    } catch (e) {
      return "";
    }
  };

  // 자동 닫힘 타이머
  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(() => {
        handleClose();
      }, duration);

      return () => clearTimeout(timer);
    }
  }, [duration]);

  // 닫기 애니메이션 처리
  const handleClose = () => {
    setIsVisible(false);
    // 애니메이션 시간을 고려해 300ms 후 실제 닫기 이벤트 발생
    setTimeout(() => {
      onClose(id);
    }, 300);
  };

  return (
    <div
      className={`max-w-sm w-full ${bgColor} border-l-4 ${textColor} p-4 shadow-md rounded-md mb-2 transform transition-all duration-300 ease-in-out ${
        isVisible ? "translate-x-0 opacity-100" : "translate-x-full opacity-0"
      }`}
    >
      <div className="flex items-start">
        <div className={`flex-shrink-0 ${iconColor}`}>{icon}</div>

        <div className="ml-3 flex-1">
          <div className="flex items-center justify-between">
            <span className="font-medium text-sm">
              {type === "COMMENT" && "댓글"}
              {type === "LIKE" && "좋아요"}
              {type === "FOLLOW" && "팔로우"}
            </span>
            <span className="text-xs">{formatDate(createdAt)}</span>
          </div>
          <p className="mt-1 text-sm">{message}</p>
        </div>

        <button
          onClick={handleClose}
          className="ml-2 text-gray-400 hover:text-gray-600 focus:outline-none"
        >
          <X size={16} />
        </button>
      </div>
    </div>
  );
};
