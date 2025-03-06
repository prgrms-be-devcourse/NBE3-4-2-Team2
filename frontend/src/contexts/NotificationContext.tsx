"use client";

import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
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
  // 알림 상태 관리
  const [notifications, setNotifications] = useState<NotificationEvent[]>([]);
  const [sseNotification, setSseNotification] =
    useState<NotificationEvent | null>(null);

  // 알림 추가 - useCallback으로 메모이제이션하여 불필요한 재생성 방지
  const addNotification = useCallback(
    (notification: NotificationEvent) => {
      setNotifications((prev) => {
        // 중복 알림 제거
        const filtered = prev.filter(
          (n) => n.notificationId !== notification.notificationId
        );
        // 최대 알림 개수 유지
        return [notification, ...filtered].slice(0, maxNotifications);
      });
    },
    [maxNotifications]
  );

  // 알림 제거
  const removeNotification = useCallback((id: number) => {
    setNotifications((prev) => prev.filter((n) => n.notificationId !== id));
  }, []);

  // 모든 알림 제거
  const clearNotifications = useCallback(() => {
    setNotifications([]);
  }, []);

  // SSE 연결 - onNotification에서는 상태만 업데이트하고 실제 처리는 별도 useEffect에서 수행
  const { connected, error } = useNotificationSSE({
    baseUrl,
    onNotification: (notification) => {
      setSseNotification(notification);
    },
  });

  // SSE로부터 받은 알림 처리를 위한 별도 useEffect
  useEffect(() => {
    if (sseNotification) {
      addNotification(sseNotification);
      setSseNotification(null);
    }
  }, [sseNotification, addNotification]);

  // 컨텍스트 값 생성 - useMemo로 최적화할 수도 있음
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
