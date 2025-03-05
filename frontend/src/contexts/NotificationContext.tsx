"use client";

import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  ReactNode,
} from "react";
import {
  useNotificationSSE,
  NotificationEvent,
} from "@/hooks/useNotificationSSE";
import { ToastNotification } from "@/components/notice/Notification";

interface NotificationContextType {
  notifications: NotificationEvent[];
  addNotification: (notification: NotificationEvent) => void;
  removeNotification: (id: number) => void;
  clearNotifications: () => void;
  connected: boolean;
  error: string | null;
}

const NotificationContext = createContext<NotificationContextType | undefined>(
  undefined
);

interface NotificationProviderProps {
  children: ReactNode;
  baseUrl?: string;
  maxNotifications?: number;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({
  children,
  baseUrl,
  maxNotifications = 5,
}) => {
  const [notifications, setNotifications] = useState<NotificationEvent[]>([]);

  // SSE 연결 훅 사용
  const { connected, error } = useNotificationSSE({
    baseUrl,
    onNotification: (notification) => {
      addNotification(notification);
    },
  });

  // 알림 추가
  const addNotification = (notification: NotificationEvent) => {
    setNotifications((prev) => {
      // 중복 알림 제거
      const filtered = prev.filter(
        (n) => n.notificationId !== notification.notificationId
      );
      // 최대 알림 개수 유지
      return [notification, ...filtered].slice(0, maxNotifications);
    });
  };

  // 알림 제거
  const removeNotification = (id: number) => {
    setNotifications((prev) => prev.filter((n) => n.notificationId !== id));
  };

  // 모든 알림 제거
  const clearNotifications = () => {
    setNotifications([]);
  };

  const contextValue: NotificationContextType = {
    notifications,
    addNotification,
    removeNotification,
    clearNotifications,
    connected,
    error,
  };

  return (
    <NotificationContext.Provider value={contextValue}>
      {children}
      <NotificationContainer />
    </NotificationContext.Provider>
  );
};

// 알림 컨테이너 - 토스트 알림을 화면 오른쪽 하단에 표시
const NotificationContainer: React.FC = () => {
  const context = useContext(NotificationContext);

  if (!context) {
    return null;
  }

  const { notifications, removeNotification } = context;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col-reverse items-end space-y-reverse space-y-2">
      {notifications.map((notification) => (
        <ToastNotification
          key={notification.notificationId}
          id={notification.notificationId}
          type={notification.type}
          message={notification.message}
          createdAt={notification.createdAt}
          onClose={removeNotification}
        />
      ))}
    </div>
  );
};

// 커스텀 훅
export const useNotifications = () => {
  const context = useContext(NotificationContext);

  if (context === undefined) {
    throw new Error(
      "useNotifications는 NotificationProvider 내에서 사용해야 합니다"
    );
  }

  return context;
};
