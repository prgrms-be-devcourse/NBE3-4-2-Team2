// 수동으로 알림 트리거 (테스트용)
"use client";

import { useNotifications } from "@/contexts/NotificationContext";

export function TestNotification() {
  const { addNotification } = useNotifications();

  const triggerTestNotification = (e: React.MouseEvent<HTMLButtonElement>) => {
    // 이벤트 전파 방지
    e.preventDefault();
    e.stopPropagation();

    addNotification({
      notificationId: Date.now(), // 임시 ID 생성
      type: "LIKE",
      targetId: 0,
      message: "테스트 알림입니다",
      createdAt: new Date().toISOString(),
    });
  };

  return (
    <button
      onClick={triggerTestNotification}
      className="px-4 py-2 bg-blue-500 text-white rounded"
    >
      테스트 알림 표시
    </button>
  );
}
