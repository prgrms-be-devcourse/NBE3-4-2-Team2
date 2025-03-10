'use client'

import Link from 'next/link';
import { Globe2, Users, MessageCircle, Star } from 'lucide-react';

export default function LandingPage() {
  return (
    <div className="min-h-screen bg-white dark:bg-gray-900 transition-colors duration-200">
      {/* 메인 콘텐츠 */}
      <div className="pt-20 px-6">
        {/* 히어로 섹션 */}
        <div className="max-w-4xl mx-auto rounded-xl p-8 md:p-12 bg-white dark:bg-gray-800 shadow-lg dark:shadow-gray-900/30 mt-8 transition-colors duration-200">
          <div className="flex flex-col md:flex-row items-center">
            <div className="md:w-1/2 mb-8 md:mb-0 flex justify-center">
              <div className="relative">
                <Globe2 className="w-40 h-40 text-blue-600 dark:text-blue-400 animate-pulse" />
                <div className="absolute w-6 h-6 bg-green-400 dark:bg-green-500 rounded-full animate-bounce" style={{top: '15%', left: '70%'}} />
                <div className="absolute w-8 h-8 bg-blue-400 dark:bg-blue-500 rounded-full animate-pulse" style={{top: '60%', left: '20%'}} />
              </div>
            </div>
            <div className="md:w-1/2 text-center md:text-left md:pl-6">
              <h1 className="text-3xl md:text-4xl font-bold mb-6 text-blue-600 dark:text-blue-400">
                글로벌과 함께하는<br />새로운 소통의 시작
              </h1>
              <p className="text-lg mb-6 text-gray-600 dark:text-gray-300">
                물리적 거리는 더 이상 장벽이 되지 않습니다. InstaKgram에서 최고의 순간을 공유하고, 소통해보세요!
              </p>
              <Link
                href="/login"
                className="inline-block bg-blue-600 dark:bg-blue-700 text-white px-8 py-3 rounded-full text-lg hover:bg-blue-700 dark:hover:bg-blue-600 transition"
              >
                시작하기
              </Link>
            </div>
          </div>
        </div>

        {/* 특징 섹션 */}
        <div className="max-w-4xl mx-auto mt-12 grid grid-cols-1 md:grid-cols-3 gap-6 mb-12">
          <div className="p-6 rounded-xl shadow-lg bg-white dark:bg-gray-800 dark:shadow-gray-900/30 text-center transition-colors duration-200">
            <Users className="w-12 h-12 mx-auto mb-4 text-blue-600 dark:text-blue-400" />
            <h3 className="text-xl font-bold mb-3 dark:text-white">글로벌 커뮤니티</h3>
            <p className="text-gray-600 dark:text-gray-300">전 세계 사용자들과 연결되어 다양한 문화와 아이디어를 공유하세요.</p>
          </div>
          <div className="p-6 rounded-xl shadow-lg bg-white dark:bg-gray-800 dark:shadow-gray-900/30 text-center transition-colors duration-200">
            <MessageCircle className="w-12 h-12 mx-auto mb-4 text-blue-600 dark:text-blue-400" />
            <h3 className="text-xl font-bold mb-3 dark:text-white">새로운 인연</h3>
            <p className="text-gray-600 dark:text-gray-300">다른 사람들의 소중한 순간들을 함께 즐겨보세요!</p>
          </div>
          <div className="p-6 rounded-xl shadow-lg bg-white dark:bg-gray-800 dark:shadow-gray-900/30 text-center transition-colors duration-200">
            <Star className="w-12 h-12 mx-auto mb-4 text-blue-600 dark:text-blue-400" />
            <h3 className="text-xl font-bold mb-3 dark:text-white">사용자 맞춤 피드</h3>
            <p className="text-gray-600 dark:text-gray-300">자신의 취향과 어울리는 피드를 추천해드립니다!</p>
          </div>
        </div>
      </div>
    </div>
  );
}