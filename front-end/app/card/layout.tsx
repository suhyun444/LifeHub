import type React from "react"
import type { Metadata } from "next"
import { GeistSans } from "geist/font/sans"
import { GeistMono } from "geist/font/mono"
import { Analytics } from "@vercel/analytics/next"
import { Suspense } from "react"
import { DataProvider } from "@/lib/data-context"
import ToastProvider from "@/components/ToastProvider"
import "./globals.css"

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
    <html lang="en">
      <body className={`font-sans ${GeistSans.variable} ${GeistMono.variable}`}>
        <DataProvider>
          <Suspense fallback={null}>{children}</Suspense>
        </DataProvider>
        <ToastProvider/>
        <Analytics />
      </body>
    </html>
  )
}
