"use client"; // 이 컴포넌트는 클라이언트 측에서 실행되어야 함을 명시

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';

const LoginSuccessHandler = () => {
  const router = useRouter(); // Next.js의 페이지 이동 훅
  const searchParams = useSearchParams(); // Next.js의 URL 파라미터 훅

  useEffect(() => {
    // 1. URL에서 'token' 파라미터를 추출합니다.
    console.log('saving token')
    const token = searchParams.get('token');
    if (token) {
      // 2. 토큰을 localStorage에 저장합니다.
      localStorage.setItem('accessToken', token);
      console.log('Token saved to localStorage.');

      window.location.href = '/card';
    } else {
      console.error('No token found in URL.');
      router.push('/login/google'); // 토큰이 없으면 로그인 페이지로
    }
  }, [router, searchParams]);

  return <div>Login in progress...</div>;
};

export default LoginSuccessHandler;