import type { Config } from "tailwindcss";

export default {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: 'class', // 다크모드 'class' 전략 추가
  theme: {
    extend: {
      colors: {
        background: "var(--background)",
        foreground: "var(--foreground)",
        // 다크모드를 위한 추가 변수
        "background-dark": "var(--background-dark)",
        "foreground-dark": "var(--foreground-dark)",
      },
    },
  },
  plugins: [],
} satisfies Config;
