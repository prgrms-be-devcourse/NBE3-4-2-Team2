import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function middleware(request: NextRequest) {
  // 쿠키에서 refresh_token 확인
  const hasRefreshToken = request.cookies.has("refresh_token");

  // refresh_token이 없고 보호된 경로 접근 시 로그인 페이지로 리다이렉트
  if (!hasRefreshToken && !request.nextUrl.pathname.startsWith("/login")) {
    return NextResponse.redirect(new URL("/login", request.url));
  }

  return NextResponse.next();
}

// 미들웨어가 적용될 경로 지정
export const config = {
  matcher: [
    "/bookmark/:path*",
    "/notice/:path*",
    "/search/:path*",
    "/post/:path*",

    // 보호하고 싶은 다른 경로들...
  ],
};
