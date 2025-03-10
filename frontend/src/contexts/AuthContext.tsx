'use client'

import client from "@/lib/backend/client";
import { createContext, useContext, useEffect, useState } from 'react';


/**
 * AuthContextType
 * AuthProvider에서 사용할 context 타입 정의
 * 전역적으로 공유될 인증 정보를 담는다.
 */
interface AuthContextType {
  isAuthenticated: boolean
  accessToken: string | null
  login: (token: string) => void
  logout: () => void
}

/**
 * AuthContext 생성
 * createContext : React Context API에서 Context를 생성한다.
 * 반환값은 AuthContextType 또는 null이다.(초기값 = null)
 */
const AuthContext = createContext<AuthContextType | null>(null)

/**
 * AuthProvider 생성
 * useState(상태관리) : accessToken(null), isLoading
 */
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)


  /**
   * AuthProvider가 처음 마운트 될 때만 실행
   */
  useEffect(() => {
    const initAuth = async () => {
      try {
        // localStorage의 토큰 체크
        const token = localStorage.getItem('accessToken')
        if (token) {
          setAccessToken(token) // 만약 토큰이 있으면 
          setIsLoading(false)
          return
        }

        // refresh token으로 access token 발급 시도
        if (document.cookie.includes('refresh_token')) { // TODO : document.cookie로 접근이 가능한지 확인
          console.log('refresh token이 존재한다. 근데 http-only인데?')
          const response = await client.GET("/api-v1/members/auth/refresh", {});
          const authorization = response.response.headers.get('Authorization');
          const accessToken = authorization?.split(' ')[1];
          
          if (accessToken) { 
            setAccessToken(accessToken)
            localStorage.setItem('accessToken', accessToken)
          }
        }
      } catch (error) {
        console.error('Auth initialization failed:', error)
      } finally {
        setIsLoading(false)
      }
    }

    initAuth()
  }, [])

  /**
   * login() : accessToken을 받아서 상태관리하고 localStorage에 저장
   * @param token : string
   */
  const login = (token: string) => {
    setAccessToken(token)
    localStorage.setItem('accessToken', token)
  }

  const logout = async () => {
    setAccessToken(null)
    localStorage.removeItem('accessToken')
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    // TODO: refresh token 삭제. 인데 http-only라서 삭제가 안됨
    await client.DELETE("/api-v1/members/logout", {});
  }

  if (isLoading) {
    return null
  }

  return (
    <AuthContext.Provider value={{ 
      isAuthenticated: !!accessToken,
      accessToken,
      login,
      logout
    }}>
      {children}
    </AuthContext.Provider>
  )
}

/**
 * Prop drilling 없이 AuthContext 사용하기 위한 hook
 * @returns AuthContext { isAuthenticated, accessToken, login, logout }
 */
export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
} 