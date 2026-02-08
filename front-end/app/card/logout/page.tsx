"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LogoutPage() {
  const router = useRouter();

  useEffect(() => {
    localStorage.removeItem("accessToken");
    localStorage.clear();

    window.location.href = "/oauth2/authorization/google";
    
  }, []);

  return (
    // 아주 잠깐 보일 화면
    <div className="flex items-center justify-center h-screen">
      <p>Logout in progress...</p>
    </div>
  );
}