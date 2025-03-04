"use client";

import { useRouter } from "next/navigation";
import { useAuth } from '@/contexts/AuthContext'
import { useEffect } from "react";
import { loginWithCredentials, loginWithRefreshToken } from '@/lib/auth';

export default function LoginForm() {
  const router = useRouter();
  const { login } = useAuth();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.target as HTMLFormElement;

    try {
      const accessToken = await loginWithCredentials(
        form.username.value,
        form.password.value
      );
      
      if (accessToken) {
        login(accessToken);
        router.replace("/");
      }
    } catch (error) {
      console.error('Login failed:', error);
      alert("로그인에 실패했습니다.");
    }
  };

  useEffect(() => {
    const isOAuth2 = new URLSearchParams(window.location.search).has('oauth2');
    
    if (isOAuth2) {
      const handleOAuth2Login = async () => {
        try {
          const accessToken = await loginWithRefreshToken();
          if (accessToken) {
            login(accessToken);
            router.replace('/');
          }
        } catch (error) {
          console.error('OAuth2 login failed:', error);
        }
      };
      
      handleOAuth2Login();
    }
  }, [router, login]);

  return (
    <div className="w-full max-w-sm p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-bold mb-6 text-center text-black">로그인</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-black">아이디</label>
          <input
            type="text"
            name="username"
            className="p-2 border rounded-md w-full text-black"
            placeholder="아이디"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-black">비밀번호</label>
          <input
            type="password"
            name="password"
            className="p-2 border rounded-md w-full text-black"
            placeholder="비밀번호"
          />
        </div>
        <div>
          <input
            type="submit"
            value="로그인"
            className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 cursor-pointer"
          />
        </div>
      </form>
      
      <div className="mt-6">
        <div className="relative">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-gray-300"></div>
          </div>
          <div className="relative flex justify-center text-sm">
            <span className="px-2 bg-white text-gray-500">간편 로그인</span>
          </div>
        </div>
        
        <div className="mt-6 space-y-3">
          <button
            onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/google'}
            className="w-full flex items-center relative px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
          >
            <img src="/google.svg" alt="Google" className="w-5 h-5 absolute left-4" />
            <span className="flex-1 text-center">Google로 계속하기</span>
          </button>
          
          <button
            onClick={() => window.location.href = 'http://localhost:8080/oauth2/authorization/naver'}
            className="w-full flex items-center relative px-4 py-2 border border-[#03C75A] rounded-md shadow-sm text-sm font-medium text-white bg-[#03C75A] hover:bg-[#02b351]"
          >
            <img src="/naver.svg" alt="Naver" className="w-5 h-5 absolute left-4" />
            <span className="flex-1 text-center">네이버로 계속하기</span>
          </button>
        </div>
      </div>
    </div>
  );
} 