'use client'

import { Header } from './Header';
import { Navigation } from './Navigation';
import { useState, useEffect } from 'react';
import { Menu } from 'lucide-react';

export function ClientLayout({ children }: { children: React.ReactNode }) {
  const [isNavOpen, setIsNavOpen] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [darkMode, setDarkMode] = useState(false);

  // 화면 크기에 따라 모바일 상태 감지
  useEffect(() => {
    const checkScreenSize = () => {
      setIsMobile(window.innerWidth < 768);
      if (window.innerWidth >= 768) {
        setIsNavOpen(false); // 데스크톱 모드에서는 nav 상태 리셋
      }
    };

    // 저장된 다크모드 설정 불러오기
    const savedDarkMode = localStorage.getItem('darkMode') === 'true';
    setDarkMode(savedDarkMode);
    
    // 다크모드 상태에 따라 HTML 요소에 클래스 적용
    if (savedDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }

    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  // 다크모드 토글 함수
  const toggleDarkMode = () => {
    const newDarkMode = !darkMode;
    setDarkMode(newDarkMode);
    localStorage.setItem('darkMode', String(newDarkMode));
    
    if (newDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  };

  return (
    <div className="flex flex-col h-screen dark:bg-gray-900 transition-colors duration-200">
      <div className="fixed top-0 left-0 right-0 z-20">
        <Header darkMode={darkMode} />
      </div>
      
      {/* 모바일에서 네비게이션이 열렸을 때 오버레이 */}
      {isMobile && isNavOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-10"
          onClick={() => setIsNavOpen(false)}
        />
      )}
      
      {/* 모바일 모드에서 펼쳐진 상태가 아닐 때만 햄버거 아이콘 표시 */}
      {isMobile && !isNavOpen && (
        <button 
          onClick={() => setIsNavOpen(true)}
          className="fixed top-[80px] left-4 z-10 bg-white dark:bg-gray-800 p-2 rounded-full shadow-md"
          aria-label="메뉴 열기"
        >
          <Menu size={24} className="text-gray-700 dark:text-gray-200" />
        </button>
      )}
      
      <div className="flex mt-[65px] h-[calc(100vh-65px)]">
        <div className={`
          fixed left-0 top-[65px] h-[calc(100vh-65px)] 
          transition-all duration-300 ease-in-out z-10
          ${isMobile ? 'w-64' : 'w-64'} 
          ${isMobile ? (isNavOpen ? 'translate-x-0' : '-translate-x-full') : 'translate-x-0'}
        `}>
          <Navigation 
            isNavOpen={isNavOpen} 
            setIsNavOpen={setIsNavOpen} 
            isMobile={isMobile}
            darkMode={darkMode}
            toggleDarkMode={toggleDarkMode}
          />
        </div>
        <div className={`
          flex-1 overflow-auto transition-all duration-300 
          ${!isMobile ? 'ml-64' : 'ml-0'}
          dark:bg-gray-900 dark:text-white
        `}>
          <main className="p-4 w-full">{children}</main>
        </div>
      </div>
    </div>
  );
}