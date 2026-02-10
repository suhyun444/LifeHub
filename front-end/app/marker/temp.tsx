import { MarkerProvider } from "@/lib/marker-context";

export default function MarkerLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <MarkerProvider>
      {children}
    </MarkerProvider>
  );
}