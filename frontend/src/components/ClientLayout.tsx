'use client'

import { Header } from './Header';
import { Navigation } from './Navigation';

export function ClientLayout({ children }: { children: React.ReactNode }) {
  return (
    <>
      <Header />
      <div className="flex">
        <Navigation />
        <main className="flex-1 p-4">{children}</main>
      </div>
    </>
  );
} 