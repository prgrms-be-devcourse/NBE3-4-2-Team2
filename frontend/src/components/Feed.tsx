"use client";

import MainFeed from "@/components/feed/MainFeed";
import { TestNotification } from "@/components/notice/TestNotification"; // 알림 테스트용

export default function Feed() {
  return (
    <main className="min-h-screen p-24 relative">
      {/* 알림 테스트 버튼 */}
      <div className="absolute top-1 right-3">
        <TestNotification />
      </div>

      <div className="flex flex-col items-center justify-center">
        <h1 className="mb-6">홈페이지</h1>

        <div className="flex justify-center">
          <div className="w-full max-w-screen-xl flex justify-center px-4">
            <MainFeed />
          </div>
        </div>
      </div>
    </main>
  );
}
