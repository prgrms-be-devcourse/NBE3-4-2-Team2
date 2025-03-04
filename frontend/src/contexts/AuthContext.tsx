'use client'

import { createContext, useContext, useState, useEffect } from 'react'
import client from "@/lib/backend/client";

interface AuthContextType {
  isAuthenticated: boolean
  accessToken: string | null
  login: (token: string) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const initAuth = async () => {
      try {
        // localStorage의 토큰 체크
        const token = localStorage.getItem('accessToken')
        if (token) {
          setAccessToken(token)
          setIsLoading(false)
          return
        }

        // refresh token으로 access token 발급 시도
        if (document.cookie.includes('refresh_token')) {
          const response = await client.GET("/api-v1/members/auth/refresh", {});
          const authorization = response.response.headers.get('Authorization');
          const newToken = authorization?.split(' ')[1];
          
          if (newToken) {
            setAccessToken(newToken)
            localStorage.setItem('accessToken', newToken)
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

  const login = (token: string) => {
    setAccessToken(token)
    localStorage.setItem('accessToken', token)
  }

  const logout = () => {
    setAccessToken(null)
    localStorage.removeItem('accessToken')
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
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

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
} 