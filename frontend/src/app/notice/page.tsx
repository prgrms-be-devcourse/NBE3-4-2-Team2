import React from "react";
import ClientPage from "./ClientPage";

export default function NotificationPage() {
  return (
    <main className="max-w-4xl mx-auto py-8 px-4">
      <h1 className="text-2xl font-bold mb-6 text-gray-900 dark:text-gray-100">알림 내역</h1>
      <ClientPage />
    </main>
  );
}
