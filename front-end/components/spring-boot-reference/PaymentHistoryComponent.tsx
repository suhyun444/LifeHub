/**
 * Spring Boot Reference: Payment History Page Component
 *
 * This component demonstrates the structure and functionality needed for a payment history page.
 * Key features to implement in Spring Boot:
 *
 * 1. Month Navigation: Left/right buttons to navigate between months
 * 2. Category Management: Add/edit categories for transactions
 * 3. Pie Chart: Visual breakdown of spending by category
 * 4. Transaction Filtering: Search and filter by category
 * 5. Transaction Details: Modal popup with detailed information
 *
 * Backend API Endpoints needed:
 * - GET /api/transactions?month=YYYY-MM&category=&search=
 * - GET /api/categories
 * - POST /api/categories
 * - PUT /api/transactions/{id}/category
 * - GET /api/transactions/{id}
 *
 * Database Tables:
 * - transactions (id, merchant, amount, date, category, status, payment_method, description)
 * - categories (id, name, created_date)
 */

"use client"

import { useState } from "react"
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
  const { transactions, categories, addCategory } = useData()
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all")
  const [currentDate, setCurrentDate] = useState(new Date())
  const [selectedTransaction, setSelectedTransaction] = useState<PaymentTransaction | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isAddingCategory, setIsAddingCategory] = useState(false)
  const [newCategoryName, setNewCategoryName] = useState("")

  const navigateMonth = (direction: "prev" | "next") => {
    const newDate = new Date(currentDate)
    if (direction === "prev") {
      newDate.setMonth(newDate.getMonth() - 1)
    } else {
      newDate.setMonth(newDate.getMonth() + 1)
    }
    setCurrentDate(newDate)
    // In Spring Boot: redirect to /history?month=YYYY-MM
  }

  const currentMonthKey = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, "0")}`

  const handleAddCategory = () => {
    if (newCategoryName.trim()) {
      addCategory(newCategoryName.trim())
      setNewCategoryName("")
      setIsAddingCategory(false)
      // In Spring Boot: POST request to /api/categories with { name: newCategoryName }
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

  const pieChartData = Object.values(categoryBreakdown)
  const COLORS = ["#8b5cf6", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#6366f1", "#ec4899"]

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

  // Utility functions - implement as Spring Boot utility classes
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
    // In Spring Boot: redirect to /transaction/{id} or open modal with AJAX call
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setSelectedTransaction(null)
  }

  return (
    <div className="min-h-screen bg-background">
      <div className="container mx-auto px-4 py-8">
        {/* Header Section */}
        <div className="flex items-center gap-4 mb-6">
          <Link href="/card">
            <Button variant="ghost" size="sm">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Back to Dashboard
            </Button>
          </Link>
          <div>
            <h1 className="text-3xl font-bold text-foreground">Payment History</h1>
            <p className="text-muted-foreground">View and manage your transaction history</p>
          </div>
        </div>

        {/* Category Management Section */}
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

        {/* Filters Section */}
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

        {/* Month Navigation and Pie Chart Section */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          {/* Month Navigation Card */}
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

          {/* Category Breakdown Pie Chart */}
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
                        paddingAngle={2}
                        dataKey="value"
                      >
                        {pieChartData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value: number) => [formatCurrency(value), "Amount"]}
                        labelFormatter={(label) => `Category: ${label}`}
                      />
                      <Legend
                        formatter={(value, entry) => (
                          <span style={{ color: entry.color }}>
                            {value} ({formatCurrency(entry.payload?.value || 0)})
                          </span>
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

        {/* Transaction List Section */}
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

        {/* Empty State */}
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

      {/* Payment Detail Modal */}
      <PaymentDetailModal transaction={selectedTransaction} isOpen={isModalOpen} onClose={handleCloseModal} />
    </div>
  )
}
