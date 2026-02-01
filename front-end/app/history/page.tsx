"use client"

import { useEffect, useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { PaymentDetailModal } from "@/components/payment-detail-modal"
import { useData } from "@/lib/data-context"
import type { PaymentTransaction } from "@/lib/mock-data"
import {
  ArrowLeft,
  Search,
  Filter,
  Calendar,
  CreditCard,
  Plus,
  Check,
  X,
  Tags,
  ChevronLeft,
  ChevronRight,
} from "lucide-react"
import Link from "next/link"
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from "recharts"

export default function PaymentHistoryPage() {
  const { transactions, categories, addCategory, importTransactions } = useData()
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all")
  const [currentDate, setCurrentDate] = useState(new Date())
  const [selectedTransaction, setSelectedTransaction] = useState<PaymentTransaction | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isAddingCategory, setIsAddingCategory] = useState(false)
  const [newCategoryName, setNewCategoryName] = useState("")

  useEffect(() => {
      setCurrentDate(currentDate);
  }, []);


  const navigateMonth = (direction: "prev" | "next") => {
    const newDate = new Date(currentDate)
    if (direction === "prev") {
      newDate.setMonth(newDate.getMonth() - 1)
    } else {
      newDate.setMonth(newDate.getMonth() + 1)
    }
    setCurrentDate(newDate)
  }

  const currentMonthKey = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, "0")}`

  const months = Array.from(
    new Set(
      transactions.map((t) => {
        const date = new Date(t.date)
        return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
      }),
    ),
  )
    .sort()
    .reverse()

  const handleAddCategory = () => {
    if (newCategoryName.trim()) {
      addCategory(newCategoryName.trim())
      setNewCategoryName("")
      setIsAddingCategory(false)
    }
  }

  const filteredTransactions = transactions.filter((transaction) => {
    const matchesSearch =
      transaction.merchant.toLowerCase().includes(searchTerm.toLowerCase()) ||
      transaction.description.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesCategory = selectedCategory === "all" || transaction.category === selectedCategory
    const transactionMonth = new Date(transaction.date).toISOString().slice(0, 7)
    const matchesMonth = transactionMonth === currentMonthKey

    return matchesSearch && matchesCategory && matchesMonth
  })

  const categoryBreakdown = filteredTransactions.reduce(
    (acc, transaction) => {
      const category = transaction.category
      if (!acc[category]) {
        acc[category] = { name: category, value: 0, count: 0 }
      }
      acc[category].value += transaction.amount
      acc[category].count += 1
      return acc
    },
    {} as Record<string, { name: string; value: number; count: number }>,
  )

  const pieChartData = [...Object.values(categoryBreakdown)].sort((a, b) => b.value - a.value);


const COLORS = [
  "#8b5cf6", "#f97316", "#10b981", "#3b82f6", "#ef4444",
  "#06b6d4", "#f59e0b", "#ec4899", "#84cc16", "#6366f1",
  "#f43f5e", "#14b8a6", "#eab308", "#d946ef", "#0ea5e9",
  "#22c55e", "#a855f7", "#f87171", "#64748b", "#78716c"
];

  const groupedTransactions = filteredTransactions.reduce(
    (acc, transaction) => {
      const date = new Date(transaction.date)
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
      const monthName = date.toLocaleDateString("en-US", { year: "numeric", month: "long" })

      if (!acc[monthKey]) {
        acc[monthKey] = {
          monthName,
          transactions: [],
          total: 0,
        }
      }

      acc[monthKey].transactions.push(transaction)
      acc[monthKey].total += transaction.amount

      return acc
    },
    {} as Record<string, { monthName: string; transactions: PaymentTransaction[]; total: number }>,
  )

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("ko-KR", {
      style: "currency",
      currency: "KRW",
    }).format(amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
    })
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed":
        return "bg-green-100 text-green-800"
      case "pending":
        return "bg-yellow-100 text-yellow-800"
      case "failed":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  const handleTransactionClick = (transaction: PaymentTransaction) => {
    setSelectedTransaction(transaction)
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setSelectedTransaction(null)
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex items-center gap-4 mb-6">
          <Button variant="ghost" size="sm" asChild>
            <Link href="/">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Dashboard
            </Link>
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-foreground">Payment History</h1>
            <p className="text-muted-foreground">View and manage your transaction history</p>
          </div>
        </div>

        {/* Category Management */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Tags className="h-5 w-5" />
              Category Management
            </CardTitle>
            <CardDescription>Manage your transaction categories</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2 mb-4">
              {categories.map((category) => (
                <Badge key={category} variant="outline" className="text-sm">
                  {category}
                </Badge>
              ))}
            </div>

            {!isAddingCategory ? (
              <Button variant="outline" size="sm" onClick={() => setIsAddingCategory(true)} className="bg-transparent">
                <Plus className="h-4 w-4 mr-2" />
                Add New Category
              </Button>
            ) : (
              <div className="flex items-center gap-2">
                <Input
                  placeholder="Enter category name"
                  value={newCategoryName}
                  onChange={(e) => setNewCategoryName(e.target.value)}
                  className="max-w-xs"
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleAddCategory()
                    } else if (e.key === "Escape") {
                      setIsAddingCategory(false)
                      setNewCategoryName("")
                    }
                  }}
                />
                <Button variant="ghost" size="sm" onClick={handleAddCategory} disabled={!newCategoryName.trim()}>
                  <Check className="h-4 w-4 text-green-600" />
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => {
                    setIsAddingCategory(false)
                    setNewCategoryName("")
                  }}
                >
                  <X className="h-4 w-4 text-red-600" />
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Filters */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Filter className="h-5 w-5" />
              Filters & Search
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search transactions..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>

              <Select value={selectedCategory} onValueChange={setSelectedCategory}>
                <SelectTrigger>
                  <SelectValue placeholder="All Categories" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Categories</SelectItem>
                  {categories.map((category) => (
                    <SelectItem key={category} value={category}>
                      {category}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <span>{filteredTransactions.length} transactions</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          {/* Month Navigation */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span className="flex items-center gap-2">
                  <Calendar className="h-5 w-5 text-accent" />
                  Current Period
                </span>
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm" onClick={() => navigateMonth("prev")}>
                    <ChevronLeft className="h-4 w-4" />
                  </Button>
                  <Button variant="outline" size="sm" onClick={() => navigateMonth("next")}>
                    <ChevronRight className="h-4 w-4" />
                  </Button>
                </div>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-center">
                <p className="text-2xl font-bold">
                  {currentDate.toLocaleDateString("en-US", { year: "numeric", month: "long" })}
                </p>
                <p className="text-sm text-muted-foreground mt-1">
                  Total: {formatCurrency(filteredTransactions.reduce((sum, t) => sum + t.amount, 0))}
                </p>
              </div>
            </CardContent>
          </Card>

          {/* Category Breakdown Chart */}
          <Card>
            <CardHeader>
              <CardTitle>Category Breakdown</CardTitle>
              <CardDescription>Spending distribution by category</CardDescription>
            </CardHeader>
            <CardContent>
              {pieChartData.length > 0 ? (
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={pieChartData}
                        cx="50%"
                        cy="50%"
                        innerRadius={40}
                        outerRadius={80}
                        paddingAngle={0}
                        dataKey="value"
                        nameKey="name"
                      >
                        {pieChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value: number, name: string) => [formatCurrency(value), name]}
                        //labelFormatter={(label) => `Category: ${label}`}
                        labelStyle={{ fontWeight: "bold", color: "#333" }}
                        contentStyle={{ borderRadius: "8px", border: "none", boxShadow: "0 4px 12px rgba(0,0,0,0.1)" }}
                      />
                      <Legend
                        verticalAlign="bottom"
                        height={80} // Legend 영역의 높이를 고정합니다 (차트 침범 방지)
                        content={() => (
                          // 1. 높이 제한(max-h-20)과 스크롤(overflow-y-auto)을 걸어줍니다.
                          // 2. grid-cols-2로 2열로 깔끔하게 줄 세웁니다.
                          <div className="mt-2 w-full px-4">
                            <ul className="grid grid-cols-4 gap-x-2 gap-y-1 max-h-20 overflow-y-auto pr-2 custom-scrollbar">
                              {pieChartData.map((entry, index) => (
                                <li key={`legend-${index}`} className="flex items-center justify-start text-xs text-gray-600 py-1">
                                  
                                  {/* 1. 색상 점 */}
                                  <span 
                                    className="block w-2 h-2 rounded-full shrink-0 mr-1.5" 
                                    style={{ backgroundColor: COLORS[index % COLORS.length] }} 
                                  />
                                  
                                  {/* 2. 카테고리 이름 + 금액을 한 덩어리로 묶어서 보여줌 */}
                                  <div className="flex items-center gap-1 truncate">
                                    <span className="font-medium text-gray-700">
                                      {entry.name}
                                    </span>
                                    <span className="text-gray-500">
                                      ({formatCurrency(entry.value)})
                                    </span>
                                  </div>

                                </li>
                              ))}
                            </ul>
                          </div>
                        )}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              ) : (
                <div className="h-64 flex items-center justify-center text-muted-foreground">
                  No transactions for this period
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Transaction Groups by Month */}
        <div className="space-y-6">
          {Object.entries(groupedTransactions)
            .sort(([a], [b]) => b.localeCompare(a))
            .map(([monthKey, monthData]) => (
              <Card key={monthKey}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <Calendar className="h-5 w-5 text-accent" />
                      {monthData.monthName}
                    </CardTitle>
                    <div className="text-right">
                      <p className="text-sm text-muted-foreground">Total Spent</p>
                      <p className="text-lg font-semibold">{formatCurrency(monthData.total)}</p>
                    </div>
                  </div>
                  <CardDescription>{monthData.transactions.length} transactions</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {monthData.transactions
                      .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
                      .map((transaction) => (
                        <div
                          key={transaction.id}
                          className="flex items-center justify-between p-4 border border-border rounded-lg hover:bg-muted/50 transition-colors cursor-pointer"
                          onClick={() => handleTransactionClick(transaction)}
                        >
                          <div className="flex items-center gap-4">
                            <div className="h-10 w-10 bg-accent/10 rounded-full flex items-center justify-center">
                              <CreditCard className="h-5 w-5 text-accent" />
                            </div>
                            <div>
                              <p className="font-medium">{transaction.merchant}</p>
                              <p className="text-sm text-muted-foreground">{transaction.description}</p>
                              <div className="flex items-center gap-2 mt-1">
                                <Badge variant="secondary" className="text-xs">
                                  {transaction.category}
                                </Badge>
                                <Badge className={`text-xs ${getStatusColor(transaction.status)}`}>
                                  {transaction.status}
                                </Badge>
                              </div>
                            </div>
                          </div>
                          <div className="text-right">
                            <p className="font-semibold text-lg">{formatCurrency(transaction.amount)}</p>
                            <p className="text-sm text-muted-foreground">{formatDate(transaction.date)}</p>
                            <p className="text-xs text-muted-foreground">{transaction.paymentMethod}</p>
                          </div>
                        </div>
                      ))}
                  </div>
                </CardContent>
              </Card>
            ))}
        </div>

        {filteredTransactions.length === 0 && (
          <Card>
            <CardContent className="py-12 text-center">
              <p className="text-muted-foreground">No transactions found for this period.</p>
              <Button
                variant="outline"
                className="mt-4 bg-transparent"
                onClick={() => {
                  setSearchTerm("")
                  setSelectedCategory("all")
                  setCurrentDate(new Date())
                }}
              >
                Reset to Current Month
              </Button>
            </CardContent>
          </Card>
        )}
      </div>
      <PaymentDetailModal transaction={selectedTransaction} isOpen={isModalOpen} onClose={handleCloseModal} />
    </div>
  )
}
