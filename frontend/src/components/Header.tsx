'use client'

import Link from "next/link";
import { useAuth } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import { logout } from '@/lib/auth';
import Image from 'next/image';

export function Header({ darkMode }: { darkMode: boolean }) {
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
    <div className={`
      flex justify-between items-center px-4 py-4 border-b
      ${darkMode 
        ? 'bg-gray-800 text-white border-gray-700' 
        : 'bg-white text-black border-gray-200'
      }
      transition-colors duration-200
    `}>
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
            className={`${darkMode ? 'hover:text-gray-300' : 'hover:text-gray-600'}`}
          >
            로그아웃
          </button>
        ) : (
          <>
            <Link 
              href="/login" 
              className={`${darkMode ? 'hover:text-gray-300' : 'hover:text-gray-600'}`}
            >
              로그인
            </Link>
            <Link 
              href="/join" 
              className={`${darkMode ? 'hover:text-gray-300' : 'hover:text-gray-600'}`}
            >
              회원가입
            </Link>
          </>
        )}
      </div>
    </div>
  );
}