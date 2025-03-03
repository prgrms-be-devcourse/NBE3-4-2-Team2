"use client";

import client from "@/lib/backend/client";
import { useRouter } from "next/navigation";

export default function JoinForm() {
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const form = e.target as HTMLFormElement;

    if (form.username.value.length === 0) {
      alert("아이디를 입력해주세요.");
      form.username.focus();

      return;
    }

    if (form.password.value.length === 0) {
      alert("비밀번호를 입력해주세요.");
      form.password.focus();

      return;
    }

    if (form.passwordConfirm.value.length === 0) {
      alert("비밀번호 확인을 입력해주세요.");
      form.passwordConfirm.focus();

      return;
    }

    if (form.password.value != form.passwordConfirm.value) {
      alert("비밀번호가 일치하지 않습니다. 다시 입력해주세요.");
      form.password.focus();

      return;
    }

    if (form.email.value.length === 0) {
      alert("이메일을 입력해주세요.");
      form.email.focus();

      return;
    }

    const response = await client.POST("/api-v1/members/join", {
      body: {
        username: form.username.value,
        password: form.password.value,
        email: form.email.value,
      },
    });

    if (!response.response.ok) {
      alert("회원가입에 실패했습니다.");
      return;
    }

    if (response.data) {
      alert("회원가입이 완료되었습니다.");
      router.replace("/login");
    }
  };

  return (
    <div className="w-full max-w-sm p-6 bg-white rounded-lg shadow-md">
      <h1 className="text-2xl font-bold mb-6 text-center text-black">회원가입</h1>
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
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-black">비밀번호 확인</label>
          <input
            type="password"
            name="passwordConfirm"
            className="p-2 border rounded-md w-full text-black"
            placeholder="비밀번호 확인"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-black">이메일</label>
          <input
            type="email"
            name="email"
            className="p-2 border rounded-md w-full text-black"
            placeholder="이메일"
          />
        </div>
        <div>
          <input
            type="submit"
            value="회원가입"
            className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600 cursor-pointer"
          />
        </div>
      </form>
    </div>
  );
}
