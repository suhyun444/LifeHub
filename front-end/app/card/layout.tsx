import type React from "react"
import type { Metadata } from "next"
import { GeistSans } from "geist/font/sans"
import { GeistMono } from "geist/font/mono"
import { Analytics } from "@vercel/analytics/next"
import { DataProvider } from "@/lib/data-context"
import ToastProvider from "@/components/ToastProvider"

export const metadata: Metadata = {
  title: "Payment History Manager",
  description: "Manage and analyze your payment history with AI insights",
  generator: "v0.app",
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <DataProvider>
        {children}
    </DataProvider>
  )
}
