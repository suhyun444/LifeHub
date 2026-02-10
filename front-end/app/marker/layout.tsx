import { MarkerProvider } from "@/lib/marker-context";
import { Suspense } from "react";

export default function MarkerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <MarkerProvider>
      <Suspense fallback={<div>Loading marker data...</div>}>
        {children}
      </Suspense>
    </MarkerProvider>
  );
}