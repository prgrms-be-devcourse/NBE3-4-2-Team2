import client from '@/lib/backend/client';

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

export async function logout() {
  await client.DELETE("/api-v1/members/logout", {});
}

function getAccessTokenFromHeader(headers: Headers) {
  const authorization = headers.get('Authorization');
  return authorization?.split(' ')[1];
} 