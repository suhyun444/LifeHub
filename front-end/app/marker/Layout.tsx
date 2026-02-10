import { MarkerProvider } from "@/lib/marker-context";

export default function MarkerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    // 마커 페이지들에 들어오면 데이터 로딩 시작!
    <MarkerProvider>
      {children}
    </MarkerProvider>
  );
}