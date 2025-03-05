import type { paths } from "@/lib/backend/apiV1/schema";
import createClient from "openapi-fetch";

const client = createClient<paths>({
  baseUrl: "http://localhost:8080",
  credentials: "include",
  headers: {
    get Authorization() { // Authorization 헤더 getter
      // 서버사이드에서는 localStorage에 접근할 수 없음
      if (typeof window === 'undefined') return '';
      
      const token = localStorage.getItem('accessToken');
      return token ? `Bearer ${token}` : '';
    }
  }
});

export default client;