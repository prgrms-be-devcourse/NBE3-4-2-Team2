'use client'

import Link from "next/link";
import { Search, Bookmark, Bell, Menu, X, Sun, Moon } from 'lucide-react';
import { useTheme } from '@/contexts/ThemeContext';

export function Navigation({ 
  isNavOpen, 
  setIsNavOpen, 
  isMobile
}: { 
  isNavOpen: boolean;
  setIsNavOpen: (isOpen: boolean) => void;
  isMobile: boolean;
}) {
  const { darkMode, toggleDarkMode } = useTheme(); // Context에서 darkMode와 토글 함수 가져오기

  return (
    <nav className="w-full h-full border-r flex flex-col bg-white dark:bg-gray-800 text-black dark:text-white border-gray-200 dark:border-gray-700 transition-colors duration-200">
      {/* 모바일 모드에서 네비게이션 상단에 햄버거 메뉴 버튼 추가 */}
      {isMobile && (
        <div className="flex justify-between items-center p-4 border-b border-gray-200 dark:border-gray-700">
          <h2 className="font-semibold">메뉴</h2>
          <button 
            onClick={() => setIsNavOpen(!isNavOpen)} 
            className="focus:outline-none"
            aria-label={isNavOpen ? "닫기" : "메뉴 열기"}
          >
            {isNavOpen ? (
              <X size={24} className="text-gray-700 dark:text-gray-200" />
            ) : (
              <Menu size={24} className="text-gray-700 dark:text-gray-200" />
            )}
          </button>
        </div>
      )}
      
      <div className="p-4 flex-1">
        <ul className="space-y-4">
          <li>
            <Link 
              href="/search" 
              className="flex items-center gap-2 p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700"
            >
              <Search size={20} />
              <span>검색</span>
            </Link>
          </li>
          <li>
            <Link 
              href="/bookmark" 
              className="flex items-center gap-2 p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700"
            >
              <Bookmark size={20} />
              <span>북마크</span>
            </Link>
          </li>
          <li>
            <Link 
              href="/notice" 
              className="flex items-center gap-2 p-2 rounded-md hover:bg-gray-100 dark:hover:bg-gray-700"
            >
              <Bell size={20} />
              <span>알림</span>
            </Link>
          </li>
        </ul>
      </div>
      
      {/* 다크모드 토글 버튼을 하단에 추가 */}
      <div className="p-4 border-t border-gray-200 dark:border-gray-700">
        <button 
          onClick={toggleDarkMode}
          className="flex items-center gap-2 p-2 w-full rounded-md hover:bg-gray-100 dark:hover:bg-gray-700"
          aria-label={darkMode ? "라이트 모드로 전환" : "다크 모드로 전환"}
        >
          {darkMode ? (
            <>
              <Sun size={20} className="text-yellow-400" />
              <span>라이트 모드</span>
            </>
          ) : (
            <>
              <Moon size={20} className="text-gray-600" />
              <span>다크 모드</span>
            </>
          )}
        </button>
      </div>
    </nav>
  );
}