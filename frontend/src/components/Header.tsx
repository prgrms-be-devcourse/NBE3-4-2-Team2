'use client'

import Link from "next/link";
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import { logout } from '@/lib/auth';

export function Header() {
  const { isAuthenticated, logout: clearAuth } = useAuth();
  const router = useRouter();

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.replace('/login');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <div className="flex justify-between items-center px-4 py-4 border-b">
      <Link href="/" className="text-xl font-bold">InstaKgram</Link>
      <div className="flex gap-4">
        {isAuthenticated ? (
          <button onClick={handleLogout} className="hover:text-gray-600">
            로그아웃
          </button>
        ) : (
          <>
            <Link href="/login">로그인</Link>
            <Link href="/join">회원가입</Link>
          </>
        )}
      </div>
    </div>
  );
} 