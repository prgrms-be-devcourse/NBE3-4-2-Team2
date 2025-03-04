'use client'

import Link from "next/link";
import { Search, Bookmark, Bell } from 'lucide-react';

export function Navigation() {
  return (
    <nav className="w-64 h-[calc(100vh-65px)] border-r p-4">
      <ul className="space-y-4">
        <li><Link href="/search" className="flex items-center gap-2 p-2 hover:bg-gray-100 rounded-md"><Search size={20} /><span>검색</span></Link></li>
        <li><Link href="/bookmark" className="flex items-center gap-2 p-2 hover:bg-gray-100 rounded-md"><Bookmark size={20} /><span>북마크</span></Link></li>
        <li><Link href="/notice" className="flex items-center gap-2 p-2 hover:bg-gray-100 rounded-md"><Bell size={20} /><span>알림</span></Link></li>
      </ul>
    </nav>
  );
} 