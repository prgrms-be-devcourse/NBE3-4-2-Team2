'use client';

import { useEffect, useState } from 'react';

// 알림 타입 정의
export type NotificationType = 'COMMENT' | 'LIKE' | 'FOLLOW';

// SSE로 받는 알림 형식
export interface NotificationEvent {
  notificationId: number;
  type: NotificationType;
  targetId: number;
  message: string;
  createdAt: string;
}

interface UseNotificationSSEProps {
  onNotification?: (notification: NotificationEvent) => void;
  baseUrl?: string;
}

export const useNotificationSSE = ({
  onNotification,
  baseUrl = 'http://localhost:8080'
}: UseNotificationSSEProps = {}) => {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let eventSource: EventSource | null = null;
    
    const connectSSE = () => {
      try {
        // EventSource 연결 생성
        const url = `${baseUrl}/api-v1/notification/subscribe`;
        eventSource = new EventSource(url, { withCredentials: true });
        
        // 연결 이벤트 핸들러
        eventSource.onopen = () => {
          console.log('SSE 연결이 열렸습니다.');
          setConnected(true);
          setError(null);
        };
        
        // 알림 메시지 수신 핸들러
        eventSource.onmessage = (event) => {
          try {
            console.log('SSE 메시지 수신:', event.data);
            const data = JSON.parse(event.data) as NotificationEvent;
            
            if (onNotification) {
              onNotification(data);
            }
          } catch (err) {
            console.error('알림 메시지 처리 중 오류:', err);
          }
        };
        
        // 오류 처리
        eventSource.onerror = (err) => {
          console.error('SSE 연결 오류:', err);
          setConnected(false);
          setError('알림 서비스 연결에 실패했습니다.');
          
          // 연결 종료 및 재연결 시도
          eventSource?.close();
          
          // 3초 후 재연결 시도
          setTimeout(connectSSE, 3000);
        };
      } catch (err) {
        console.error('SSE 연결 초기화 오류:', err);
        setError('알림 서비스에 연결할 수 없습니다.');
      }
    };
    
    // 최초 연결 시도
    connectSSE();
    
    // 컴포넌트 언마운트 시 연결 정리
    return () => {
      if (eventSource) {
        console.log('SSE 연결 종료');
        eventSource.close();
        setConnected(false);
      }
    };
  }, [baseUrl, onNotification]);
  
  return { connected, error };
};