/**
 * JWT 토큰에서 페이로드를 추출하고 파싱합니다.
 * @param token JWT 토큰 문자열
 * @returns 파싱된 JWT 페이로드 또는 null (토큰이 유효하지 않은 경우)
 */
export function parseJwt(token: string): any {
    try {
      // JWT의 두 번째 부분(페이로드)을 추출
      const base64Payload = token.split('.')[1];
      // base64url 디코딩
      const payload = Buffer.from(base64Payload, 'base64').toString('utf8');
      // JSON 파싱
      return JSON.parse(payload);
    } catch (error) {
      console.error('JWT 파싱 중 오류 발생:', error);
      return null;
    }
  }
  
  /**
   * 로컬 스토리지나 쿠키에서 JWT 토큰을 가져옵니다.
   * 실제 애플리케이션에서는 토큰 저장 방식에 맞게 수정해야 합니다.
   * @returns JWT 토큰 또는 null
   */
  export function getToken(): string | null {
    if (typeof window !== 'undefined') {
      return localStorage.getItem('accessToken');
    }
    return null;
  }
  
  /**
   * 현재 로그인한 사용자의 ID를 가져옵니다.
   * @returns 사용자 ID 또는 null (인증되지 않은 경우)
   */
  export function getCurrentUserId(): number | null {
    const token = getToken();
    if (!token) return null;
    
    const payload = parseJwt(token);
    return payload?.id || null;
  }
  
  /**
   * 현재 로그인한 사용자의 사용자명을 가져옵니다.
   * @returns 사용자명 또는 null (인증되지 않은 경우)
   */
  export function getCurrentUsername(): string | null {
    const token = getToken();
    if (!token) return null;
    
    const payload = parseJwt(token);
    return payload?.sub || null;
  }
  
  /**
   * 사용자 인증 정보를 한 번에 가져옵니다.
   * @returns {id, username} 객체 또는 null (인증되지 않은 경우)
   */
  export function getCurrentUser(): { id: number; username: string } | null {
    const token = getToken();
    if (!token) return null;
    
    const payload = parseJwt(token);
    if (!payload || !payload.id) return null;
    
    return {
      id: payload.id,
      username: payload.sub
    };
  }
  
  /**
   * 토큰이 만료되었는지 확인합니다.
   * @returns true(만료됨) 또는 false(유효함)
   */
  export function isTokenExpired(): boolean {
    const token = getToken();
    if (!token) return true;
    
    const payload = parseJwt(token);
    if (!payload || !payload.exp) return true;
    
    // 현재 시간(초)과 만료 시간 비교
    const currentTime = Math.floor(Date.now() / 1000);
    return payload.exp < currentTime;
  }