'use client'

import Link from "next/link";
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import { logout } from '@/lib/auth';
import { Globe2 } from 'lucide-react';

export function Header() {
  const { isAuthenticated, logout: clearAuth } = useAuth();
  const router = useRouter();

  const handleLogout = async () => {
    try {
      await logout();
      clearAuth();
      router.replace('/');
    } catch (error) {
      console.error('Logout failed:', error);
    }
  };

  return (
    <div className="flex justify-between items-center px-4 py-4 border-b">
      <Link href="/" className="text-2xl font-bold flex items-center">
        <Globe2 className="mr-2 text-blue-600" />
        <span>InstaKgram</span>
      </Link>
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