"use client"

import type React from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useData } from "@/lib/data-context"
import { api } from "@/lib/api"
import { ArrowLeft, Upload, Download, Trash2, AlertTriangle } from "lucide-react"
import Link from "next/link"
import { toast } from 'react-toastify'

export default function DataManagementPage() {
  const { transactions, exportTransactions, importTransactions, clearAllData } = useData()

  const handleExport = () => {
    const dataStr = exportTransactions()
    const dataBlob = new Blob([dataStr], { type: "application/json" })
    const url = URL.createObjectURL(dataBlob)
    const link = document.createElement("a")
    link.href = url
    link.download = `payment-history-${new Date().toISOString().split("T")[0]}.json`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    
  }

  const handleImport = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (!file) return

    const formData = new FormData();
    formData.append("file", file);

    try {
      const data = await api.post("/api/transactions/upload", formData);
      console.log(data.transactions);
      importTransactions(data.transactions);
      toast.success("Import complete.")
    } catch (error) {
      toast.error("Import failed.")
    }
  }

  const handleClearData = async () => {
    if (window.confirm("Are you sure you want to delete all transaction data? This action cannot be undone.")) {
      try 
      {
        await api.delete(`/api/transactions/clear`);
        clearAllData()
        toast.success("Data Cleared")
        window.location.reload(); // 혹은 리스트 상태 초기화 함수 호출

      } catch (error) {
        console.error(error);
        toast.error("Clear Failed");
      }
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: "KRW",
    }).format(amount)
  }

  const totalAmount = transactions.reduce((sum, t) => sum + t.amount, 0)

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex items-center gap-4 mb-6">
          <Button variant="ghost" size="sm" asChild>
            <Link href="/card">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Dashboard
            </Link>
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-foreground">Data Management</h1>
            <p className="text-muted-foreground">Manage your payment history data</p>
          </div>
        </div>

        {/* Data Overview */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Total Transactions</p>
                  <p className="text-2xl font-bold">{transactions.length}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Total Amount</p>
                  <p className="text-2xl font-bold">{formatCurrency(totalAmount)}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-muted-foreground">Average Transaction</p>
                  <p className="text-2xl font-bold">
                    {transactions.length > 0 ? formatCurrency(totalAmount / transactions.length) : "$0.00"}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Data Import/Export */}
        <div className="max-w-4xl mx-auto">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Upload className="h-5 w-5 text-accent" />
                Import & Export
              </CardTitle>
              <CardDescription>Backup and restore your payment data</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <Label>Import Data</Label>
                    <div className="mt-1">
                      <input id="import-file" type="file" accept=".xls, .xlsx" onChange={handleImport} className="hidden" />
                      <Button
                        onClick={() => document.getElementById("import-file")?.click()}
                        variant="outline"
                        className="w-full bg-transparent"
                      >
                        <Upload className="h-4 w-4 mr-2" />
                        Import Data
                      </Button>
                    </div>
                    <p className="text-xs text-muted-foreground mt-1">Import transactions from a JSON file</p>
                  </div>

                  <Button onClick={handleExport} variant="outline" className="w-full bg-transparent">
                    <Download className="h-4 w-4 mr-2" />
                    Export Data
                  </Button>
                </div>

                <div className="space-y-4">
                  <div className="flex items-center gap-2 mb-2">
                    <AlertTriangle className="h-4 w-4 text-destructive" />
                    <span className="font-medium text-destructive">Danger Zone</span>
                  </div>
                  <Button onClick={handleClearData} variant="destructive" className="w-full">
                    <Trash2 className="h-4 w-4 mr-2" />
                    Clear All Data
                  </Button>
                  <p className="text-xs text-muted-foreground">
                    This will permanently delete all your transaction data
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
