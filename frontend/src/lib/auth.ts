/**
 * 인증정보( username password, refresh token)를 사용하여 로그인을 시도합니다.
 * return : 액세스 토큰(Nullable) - 헤더에서 추출
 */
import client from '@/lib/backend/client';





/**
 * 사용자 이름과 비밀번호로 로그인을 시도합니다.
 * @param username 사용자 이름
 * @param password 비밀번호
 * @returns 액세스 토큰(Nullable)
 */
export async function loginWithCredentials(username: string, password: string) {
  const response = await client.POST("/api-v1/members/login", {
    body: { username, password },
  });

  if (!response.response.ok) {
    throw new Error("Login failed");
  }

  return getAccessTokenFromHeader(response.response.headers);
}



export async function loginWithRefreshToken() {
  const response = await client.GET("/api-v1/members/auth/refresh", {});
  
  if (!response.response.ok) {
    throw new Error("Token refresh failed");
  }

  return getAccessTokenFromHeader(response.response.headers);
}

/**
 * 로그아웃을 시도합니다.
 */
export async function logout() {
  await client.DELETE("/api-v1/members/logout", {});
}


/**
 * 헤더에서 액세스 토큰을 추출합니다.
 */
function getAccessTokenFromHeader(headers: Headers) {
  const authorization = headers.get('Authorization');
  return authorization?.split(' ')[1];
} 