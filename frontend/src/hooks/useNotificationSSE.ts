'use client';
import { useEffect, useState, useRef, useCallback } from 'react';
import { EventSourcePolyfill } from 'event-source-polyfill';
import { getCurrentUserId } from '@/utils/jwtUtils'; // auth 유틸리티 import 추가

export type NotificationType = 'COMMENT' | 'LIKE' | 'FOLLOW';

export interface NotificationEvent {
  notificationId: number;
  type: NotificationType;
  targetId: number;
  message: string;
  createdAt: string;
}

interface UseNotificationSSEProps {
  userId?: number; // 선택적 파라미터로 변경 (직접 제공하거나 토큰에서 가져오기)
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
  userId: providedUserId, // 외부에서 제공된 userId (선택적)
  onNotification,
  baseUrl = 'http://localhost:8080'
}: UseNotificationSSEProps = {}) => {
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // 사용자 ID 추적 (props에서 제공되거나 토큰에서 추출)
  const [userId, setUserId] = useState<number | null>(providedUserId || null);
  
  const onNotificationRef = useRef(onNotification);
  const eventSourceRef = useRef<EventSourcePolyfill | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // 컴포넌트 시작 부분에 추가
useEffect(() => {
  const originalConsoleError = console.error;
  
  console.error = (...args) => {
    const errorMessage = args[0]?.toString() || '';
    if (
      errorMessage.includes('EventSource') && 
      errorMessage.includes('status 503') && 
      errorMessage.includes('Aborting the connection')
    ) {
      // 이 메시지는 출력하지 않음
      return;
    }
    
    // 다른 에러 메시지는 정상적으로 출력
    originalConsoleError.apply(console, args);
  };
  
  // 클린업 함수에서 원래 console.error 복원
  return () => {
    console.error = originalConsoleError;
  };
}, []);
  
  // onNotification 콜백 최신 상태 유지
  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);
  
  // 컴포넌트 마운트 시 토큰에서 userId 추출
  useEffect(() => {
    // 외부에서 제공된 userId가 있으면 사용
    if (providedUserId) {
      setUserId(providedUserId);
      return;
    }
    
    // 없으면 토큰에서 사용자 ID 가져오기
    const currentUserId = getCurrentUserId();
    if (currentUserId) {
      setUserId(currentUserId);
    } else {
      setUserId(null);
      setError('사용자 ID를 찾을 수 없습니다');
    }
  }, [providedUserId]);

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
    
    if (!userId) {
      console.log('userId 없음 - 연결 시도 중단');
      setError('사용자 ID가 필요합니다');
      return;
    }
    
    try {
      // 브라우저 정보 가져오기
      const browserInfo = extractBrowserInfo(navigator.userAgent);
      const encodedBrowserInfo = encodeURIComponent(browserInfo);
      const url = `${baseUrl}/api-v1/notification/subscribe?userId=${userId}&browserName=${encodedBrowserInfo}`;
      
      console.log(`${browserInfo} 브라우저로 SSE 연결 시도`);
      
      // EventSourcePolyfill 생성 (인증 헤더 제거)
      eventSourceRef.current = new EventSourcePolyfill(url, {
        withCredentials: true,  // 쿠키는 계속 전송 (필요시 제거 가능)
        heartbeatTimeout: 1000 * 60 * 10 // 10분
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
      eventSourceRef.current.onerror = (err: any) => {
      
        // 503 상태 코드 확인 후 즉시 연결 종료
        if (err?.status === 503) {
        
          reconnectTimeoutRef.current = setTimeout(connectSSE, 3000);
          console.log("새 커넥션을 요청합니다")
          return;
        }
        setConnected(false);
        setError('알림 서비스 연결에 실패했습니다');
      
      };
      
    } catch (err) {
      console.error('SSE 초기화 오류:', err);
      setError('알림 서비스에 연결할 수 없습니다');
      
      // 오류 발생 시 재연결 예약
      reconnectTimeoutRef.current = setTimeout(connectSSE, 3000);
    }
  }, [userId, baseUrl, disconnectSSE]);

  // userId가 변경되면 연결 시작/재시작
  useEffect(() => {
    if (userId) {
      connectSSE();
    } else {
      disconnectSSE();
    }
    
    return () => {
      disconnectSSE();
    };
  }, [userId, connectSSE, disconnectSSE]);
  
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
        if (userId && !eventSourceRef.current) {
          console.log('연결이 없음 - 재연결 시도');
          connectSSE();
        }
      }
    };
    
    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [userId, connectSSE]);
  
  return { connected, error, userId };
};