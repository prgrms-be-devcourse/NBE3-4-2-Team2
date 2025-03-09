'use client';
import { useEffect, useState, useRef, useCallback } from 'react';
import { EventSourcePolyfill } from 'event-source-polyfill';
import { useAuth } from '@/contexts/AuthContext';

export type NotificationType = 'COMMENT' | 'LIKE' | 'FOLLOW';

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

// 브라우저 정보 추출 함수
const extractBrowserInfo = (userAgent: string): string => {
  if (userAgent.indexOf("Chrome") > -1) return "Chrome";
  if (userAgent.indexOf("Firefox") > -1) return "Firefox";
  if (userAgent.indexOf("Safari") > -1 && userAgent.indexOf("Chrome") === -1) return "Safari";
  if (userAgent.indexOf("Edge") > -1) return "Edge";
  if (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("Trident") > -1) return "IE";
  return "Other";
};

export const useNotificationSSE = ({
  onNotification,
  baseUrl = 'http://localhost:8080'
}: UseNotificationSSEProps = {}) => {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { isAuthenticated, accessToken } = useAuth();
  
  const onNotificationRef = useRef(onNotification);
  const eventSourceRef = useRef<EventSourcePolyfill | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  
  // onNotification 콜백 최신 상태 유지
  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);

  // 연결 종료 함수
  const disconnectSSE = useCallback(() => {
    if (eventSourceRef.current) {
      console.log('SSE 연결 종료');
      eventSourceRef.current.close();
      eventSourceRef.current = null;
      setConnected(false);
    }
    
    // 예약된 재연결 취소
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
  }, []);

  // 연결 함수
  const connectSSE = useCallback(() => {
    console.log('SSE 연결 시작');
    
    // 기존 연결이 있으면 먼저 종료
    disconnectSSE();
    
    if (!accessToken) {
      console.log('토큰 없음 - 연결 시도 중단');
      return;
    }
    
    try {
      // 브라우저 정보 가져오기
      const browserInfo = extractBrowserInfo(navigator.userAgent);
      const encodedBrowserInfo = encodeURIComponent(browserInfo);
      const url = `${baseUrl}/api-v1/notification/subscribe?browserName=${encodedBrowserInfo}`;
      
      console.log(`${browserInfo} 브라우저로 SSE 연결 시도`);
      
      // EventSourcePolyfill 생성
      eventSourceRef.current = new EventSourcePolyfill(url, {
        headers: {
          'Authorization': `Bearer ${accessToken}`
        },
        withCredentials: true
      });
      
      // 연결 성공 핸들러
      eventSourceRef.current.onopen = () => {
        console.log('SSE 연결 성공');
        setConnected(true);
        setError(null);
      };
      
      // 메시지 수신 핸들러
      eventSourceRef.current.onmessage = (event) => {
        try {
          const data = event.data;
          console.log('메시지 수신:', data);
          
          // 종료 메시지 처리
          if (data === 'close') {
            console.log('서버에서 연결 종료 요청');
            disconnectSSE();
            return;
          }
          
          // 하트비트 메시지 처리
          if (data === 'thump' || data === 'heartbeat') {
            console.log('하트비트 수신');
            return;
          }
          
          // 일반 알림 처리
          const notificationData = JSON.parse(data) as NotificationEvent;
          if (onNotificationRef.current) {
            onNotificationRef.current(notificationData);
          }
        } catch (err) {
          console.error('메시지 처리 중 오류:', err);
        }
      };
      
      // 에러 핸들러
      eventSourceRef.current.onerror = (err) => {
        console.error('SSE 연결 오류 발생');
        
        // 연결 상태 업데이트
        setConnected(false);
        setError('알림 서비스 연결에 실패했습니다');
        
        // 연결 종료
        if (eventSourceRef.current) {
          eventSourceRef.current.close();
          eventSourceRef.current = null;
        }
        
        // 재연결 예약 (3초 후)
        reconnectTimeoutRef.current = setTimeout(() => {
          console.log('연결 재시도 중...');
          connectSSE();
        }, 3000);
      };
      
    } catch (err) {
      console.error('SSE 초기화 오류:', err);
      setError('알림 서비스에 연결할 수 없습니다');
      
      // 오류 발생 시 재연결 예약
      reconnectTimeoutRef.current = setTimeout(connectSSE, 3000);
    }
  }, [accessToken, baseUrl, disconnectSSE]);

  // 인증 상태 변경 감지
  useEffect(() => {
    if (isAuthenticated && accessToken) {
      connectSSE();
    } else {
      disconnectSSE();
    }
    
    return () => {
      disconnectSSE();
    };
  }, [isAuthenticated, accessToken, connectSSE, disconnectSSE]);
  
  // 페이지 언로드 이벤트 처리
  useEffect(() => {
    const handleBeforeUnload = () => {
      console.log('페이지 종료 감지 - 연결 종료');
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };
    
    window.addEventListener('beforeunload', handleBeforeUnload);
    
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, []);
  
  // 페이지 가시성 변경 감지
  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.hidden) {
        console.log('페이지 숨김 - 연결 유지');
      } else {
        console.log('페이지 표시됨');
        // 연결이 끊어진 상태라면 재연결 시도
        if (isAuthenticated && accessToken && !eventSourceRef.current) {
          console.log('연결이 없음 - 재연결 시도');
          connectSSE();
        }
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [isAuthenticated, accessToken, connectSSE]);
  
  return { connected, error };
};