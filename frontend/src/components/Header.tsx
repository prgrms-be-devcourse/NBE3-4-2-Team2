"use client";

import Link from "next/link";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { logout } from "@/lib/auth";
import { Globe2 } from "lucide-react";
import Image from "next/image";
import { useTheme } from "@/contexts/ThemeContext";

export function Header() {
  const { darkMode } = useTheme(); // Context에서 darkMode 가져오기
  const { isAuthenticated, logout: clearAuth } = useAuth();
  const router = useRouter();

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.replace("/");
    } catch (error) {
      console.error("Logout failed:", error);
    }
  };

  return (
    <div
      className={`
      flex justify-between items-center px-4 py-4 border-b
      bg-white dark:bg-gray-800 text-black dark:text-white 
      border-gray-200 dark:border-gray-700
      transition-colors duration-200
    `}
    >
      <Link href="/" className="text-2xl font-bold flex items-center">
        <div className="relative w-12 h-12 mr-2">
          <Image
            src="/logo.svg"
            alt="InstaKgram Logo"
            fill
            className="object-contain"
          />
        </div>
        <span>InstaKgram</span>
      </Link>
      <div className="flex gap-4">
        {isAuthenticated ? (
          <button
            onClick={handleLogout}
            className="hover:text-gray-600 dark:hover:text-gray-300"
          >
            로그아웃
          </button>
        ) : (
          <>
            <Link
              href="/login"
              className="hover:text-gray-600 dark:hover:text-gray-300"
            >
              로그인
            </Link>
            <Link
              href="/join"
              className="hover:text-gray-600 dark:hover:text-gray-300"
            >
              회원가입
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
