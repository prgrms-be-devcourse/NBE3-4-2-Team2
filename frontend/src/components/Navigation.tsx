'use client'

import Link from "next/link";
import { Search, Bookmark, Bell } from 'lucide-react';

export function Navigation() {
  return (
    <nav className="w-64 h-[calc(100vh-65px)] border-r p-4">
      <ul className="space-y-4">
        <li>
          <Link 
            href="/search" 
            className="flex items-center gap-2 p-3 hover:bg-blue-50 rounded-md transition-all group"
          >
            <Search 
              size={24} 
              className="text-gray-100 group-hover:text-blue-600 transition-colors" 
            />
            <span className="font-semibold text-gray-100 group-hover:text-blue-600 transition-colors">
              검색
            </span>
          </Link>
        </li>
        <li>
          <Link 
            href="/bookmark" 
            className="flex items-center gap-2 p-3 hover:bg-blue-50 rounded-md transition-all group"
          >
            <Bookmark 
              size={24} 
              className="text-gray-100 group-hover:text-blue-600 transition-colors" 
            />
            <span className="font-semibold text-gray-100 group-hover:text-blue-600 transition-colors">
              북마크
            </span>
          </Link>
        </li>
        <li>
          <Link 
            href="/notice" 
            className="flex items-center gap-2 p-3 hover:bg-blue-50 rounded-md transition-all group"
          >
            <Bell 
              size={24} 
              className="text-gray-100 group-hover:text-blue-600 transition-colors" 
            />
            <span className="font-semibold text-gray-100 group-hover:text-blue-600 transition-colors">
              알림
            </span>
          </Link>
        </li>
      </ul>
    </nav>
  );
} 