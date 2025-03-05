'use client'

import Link from "next/link";
import { Search, Bookmark, Bell, Menu, X, Sun, Moon } from 'lucide-react';

export function Navigation({ 
  isNavOpen, 
  setIsNavOpen, 
  isMobile, 
  darkMode, 
  toggleDarkMode 
}: { 
  isNavOpen: boolean;
  setIsNavOpen: (isOpen: boolean) => void;
  isMobile: boolean;
  darkMode: boolean;
  toggleDarkMode: () => void;
}) {
  return (
    <nav className={`
      w-full h-full border-r flex flex-col
      ${darkMode 
        ? 'bg-gray-800 text-white border-gray-700' 
        : 'bg-white text-black border-gray-200'
      }
      transition-colors duration-200
    `}>
      {/* 모바일 모드에서 네비게이션 상단에 햄버거 메뉴 버튼 추가 */}
      {isMobile && (
        <div className={`
          flex justify-between items-center p-4 border-b
          ${darkMode ? 'border-gray-700' : 'border-gray-200'}
        `}>
          <h2 className="font-semibold">메뉴</h2>
          <button 
            onClick={() => setIsNavOpen(!isNavOpen)} 
            className="focus:outline-none"
            aria-label={isNavOpen ? "닫기" : "메뉴 열기"}
          >
            {isNavOpen ? (
              <X size={24} className={darkMode ? "text-gray-200" : "text-gray-700"} />
            ) : (
              <Menu size={24} className={darkMode ? "text-gray-200" : "text-gray-700"} />
            )}
          </button>
        </div>
      )}
      
      <div className="p-4 flex-1">
        <ul className="space-y-4">
          <li>
            <Link 
              href="/search" 
              className={`
                flex items-center gap-2 p-2 rounded-md
                ${darkMode 
                  ? 'hover:bg-gray-700' 
                  : 'hover:bg-gray-100'
                }
              `}
            >
              <Search size={20} />
              <span>검색</span>
            </Link>
          </li>
          <li>
            <Link 
              href="/bookmark" 
              className={`
                flex items-center gap-2 p-2 rounded-md
                ${darkMode 
                  ? 'hover:bg-gray-700' 
                  : 'hover:bg-gray-100'
                }
              `}
            >
              <Bookmark size={20} />
              <span>북마크</span>
            </Link>
          </li>
          <li>
            <Link 
              href="/notice" 
              className={`
                flex items-center gap-2 p-2 rounded-md
                ${darkMode 
                  ? 'hover:bg-gray-700' 
                  : 'hover:bg-gray-100'
                }
              `}
            >
              <Bell size={20} />
              <span>알림</span>
            </Link>
          </li>
        </ul>
      </div>
      
      // 다크/라이트 모드 전환 최하단
      <div className={`
        p-4 border-t
        ${darkMode ? 'border-gray-700' : 'border-gray-200'}
      `}>
        <button 
          onClick={toggleDarkMode}
          className={`
            flex items-center gap-2 p-2 w-full rounded-md
            ${darkMode 
              ? 'hover:bg-gray-700' 
              : 'hover:bg-gray-100'
            }
          `}
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