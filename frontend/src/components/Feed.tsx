'use client'

import MainFeed from "@/components/feed/MainFeed";

export default function Feed() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <div className="flex justify-center">
        <div className="w-full max-w-screen-xl flex justify-center px-4">
          <MainFeed />
        </div>
      </div>
    </main>
  );
} 