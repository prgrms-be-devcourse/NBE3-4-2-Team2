'use client';

import { useEffect, useState, useRef } from 'react';

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
  
  // onNotification 콜백을 ref로 관리하여 의존성 배열에서 제거
  const onNotificationRef = useRef(onNotification);
  
  // onNotification이 변경될 때만 ref 업데이트
  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);

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
            
            // ref를 통해 최신 콜백 함수 호출
            if (onNotificationRef.current) {
              onNotificationRef.current(data);
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
  }, [baseUrl]); // 의존성 배열에서 onNotification 제거
  
  return { connected, error };
};