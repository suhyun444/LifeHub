"use client"

import { useState, useEffect, useMemo, useRef } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { useData } from "@/lib/data-context"
import { api } from "@/lib/api"
// 🔹 RefreshCw(재분석 아이콘) 추가
import { ArrowLeft, Brain, TrendingUp, TrendingDown, AlertCircle, Target, PieChart, BarChart3, ChevronLeft, ChevronRight, Sparkles, Loader2, RefreshCw } from "lucide-react"
import Link from "next/link"
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts"

// 🔹 데이터 타입 정의
type Trend = {
  type: "increase" | "decrease" | "stable"
  category: string
  change: string
  description: string
}

type Recommendation = {
  title: string
  description: string
  priority: "high" | "medium" | "low"
}

type AIInsight = {
  month: string // 식별자
  summary: string
  trends: Trend[]
  recommendations: Recommendation[]
  budgetHealth: {
    score: number
    status: string
    description: string
  }
}

export default function AIAnalysisPage() {
  const { transactions } = useData()
  
  // 날짜 관련 헬퍼 함수
  const getMonthKey = (date: Date) => {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
  }
  
  const [selectedMonth, setSelectedMonth] = useState<string>(getMonthKey(new Date()))
  
  // 🔹 1. 기능 변경: 단일 객체 대신 배열로 이력 관리
  const [insightsHistory, setInsightsHistory] = useState<AIInsight[]>([])
  const [isLoading, setIsLoading] = useState(false)       // 초기 로딩
  const [isReanalyzing, setIsReanalyzing] = useState(false) // 재분석 로딩
  const hasFetchedHistory = useRef(false)

  // 🔹 2. 초기 자동 로딩 (페이지 진입 시 실행)
useEffect(() => {
    const fetchAllHistory = async () => {
      setIsLoading(true)
      try {
        const historyList = await api.get("/api/analysis")
        
        if (historyList && Array.isArray(historyList)) {
          setInsightsHistory(historyList)
        }
      } catch (error) {
        console.error("Failed to fetch analysis history:", error)
      } finally {
        setIsLoading(false)
      }
    }

    // 트랜잭션 데이터가 로드된 상태라면 실행
    if (transactions.length > 0 && !hasFetchedHistory.current) {
      hasFetchedHistory.current = true
      fetchAllHistory()
    }
    // selectedMonth 의존성 제거! (달이 바뀌어도 API 호출 안 함)
  }, [transactions.length])

  const handleReanalyze = () => {
    setIsReanalyzing(true)
    setTimeout(async () => {

      const filteredTransactions = transactions.filter((transaction) => {
        const date = new Date(transaction.date);

        const currentKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`

        return currentKey == selectedMonth
      })

      const updatedInsight :AIInsight = {month: selectedMonth, ...await api.post("/api/analysis", {transactions: filteredTransactions,month:selectedMonth})}

      setInsightsHistory(prev => {
        const index = prev.findIndex(item => item.month === selectedMonth)
        if (index !== -1) {
          const newHistory = [...prev]
          newHistory[index] = updatedInsight
          return newHistory
        }
        return [...prev, updatedInsight]
      })
      setIsReanalyzing(false)
    }, 1500)
  }

  // 🔹 4. 현재 선택된 달의 Insight 추출 (배열에서 find)
  const currentInsight = useMemo(() => {
    return insightsHistory.find(item => item.month === selectedMonth)
  }, [insightsHistory, selectedMonth])

  // --- 기존 데이터 계산 로직 유지 ---
  const monthlyData = transactions.reduce(
    (acc, transaction) => {
      const date = new Date(transaction.date)
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
      if (!acc[monthKey]) {
        acc[monthKey] = { month: monthKey, total: 0, transactions: 0, categories: {} }
      }
      acc[monthKey].total += transaction.amount
      acc[monthKey].transactions += 1
      if (!acc[monthKey].categories[transaction.category]) {
        acc[monthKey].categories[transaction.category] = 0
      }
      acc[monthKey].categories[transaction.category] += transaction.amount
      return acc
    },
    {} as Record<string, any>,
  )

  const currentMonthData = monthlyData[selectedMonth]

  const categoryData = currentMonthData
    ? Object.entries(currentMonthData.categories)
        .map(([category, amount]) => ({
          category,
          amount: Number(amount),
          percentage: Math.round((Number(amount) / currentMonthData.total) * 100),
        }))
        .sort((a, b) => b.amount - a.amount)
    : []

  const handleMonthChange = (increment: number) => {
    const [year, month] = selectedMonth.split("-").map(Number)
    const date = new Date(year, month - 1 + increment, 1)
    const newKey = getMonthKey(date);
    setSelectedMonth(newKey)
    // 달이 바뀌어도 배열에 저장된 데이터가 있으면 즉시 보여짐 (Loading 불필요)
  }

  // --- 스타일 헬퍼 함수 유지 ---
  const COLORS = ["#8b5cf6", "#6b7280", "#ea580c", "#dc2626", "#f97316"]
  const formatCurrency = (amount: number) => new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW" }).format(amount)
  
  const getTrendIcon = (type: string) => {
    switch (type) {
      case "increase": return <TrendingUp className="h-4 w-4 text-red-500" />
      case "decrease": return <TrendingDown className="h-4 w-4 text-green-500" />
      default: return <BarChart3 className="h-4 w-4 text-blue-500" />
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "high": return "bg-red-100 text-red-800 border-red-200"
      case "medium": return "bg-yellow-100 text-yellow-800 border-yellow-200"
      case "low": return "bg-green-100 text-green-800 border-green-200"
      default: return "bg-gray-100 text-gray-800 border-gray-200"
    }
  }

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
            <h1 className="text-3xl font-bold text-foreground flex items-center gap-2">
              <Brain className="h-8 w-8 text-accent" />
              AI Spending Analysis
            </h1>
            <p className="text-muted-foreground">Intelligent insights into your spending patterns</p>
          </div>
        </div>

        {/* Month Selector */}
        <div className="flex items-center justify-center gap-6 mb-8">
          <Button 
            variant="outline" 
            size="icon" 
            onClick={() => handleMonthChange(-1)}
            className="h-10 w-10 rounded-full hover:bg-slate-100"
          >
            <ChevronLeft className="h-6 w-6" />
          </Button>

          <div className="text-center min-w-[200px]">
            <h2 className="text-2xl font-bold text-gray-800">
              {new Date(selectedMonth + "-01").toLocaleDateString("en-US", { year: "numeric", month: "long" })}
            </h2>
            <p className="text-sm text-gray-500">Monthly Report</p>
          </div>

          <Button 
            variant="outline" 
            size="icon" 
            onClick={() => handleMonthChange(1)}
            className="h-10 w-10 rounded-full hover:bg-slate-100"
            disabled={selectedMonth === getMonthKey(new Date())}
          >
            <ChevronRight className="h-6 w-6" />
          </Button>
        </div>

        {/* 메인 컨텐츠 영역 */}
        {isLoading ? (
          /* 1. 전체 로딩 상태 (스켈레톤 대신 직관적인 로딩 화면) */
          <div className="flex flex-col items-center justify-center py-32">
             <Loader2 className="h-12 w-12 text-primary animate-spin mb-4" />
             <p className="text-lg text-muted-foreground">Analyzing your financial data...</p>
          </div>
        ) : (
          /* 2. 데이터 로딩 완료 */
          currentMonthData ? (
            <>
              {/* AI 데이터가 있을 때 표시 */}
              {currentInsight ? (
                <>
                  {/* AI Summary - 재분석 버튼 추가됨 */}
                  <Card className="mb-6 border-l-4 border-l-purple-500 animate-in fade-in slide-in-from-bottom-4 duration-500">
                    <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                      <CardTitle className="flex items-center gap-2">
                        <Brain className="h-5 w-5 text-purple-600" />
                        AI Summary
                      </CardTitle>
                      {/* 🔹 재분석 버튼: 디자인을 유지하며 우측 상단에 배치 */}
                      <Button 
                        variant="ghost" 
                        size="sm" 
                        onClick={handleReanalyze} 
                        disabled={isReanalyzing}
                        className="h-8 text-xs text-muted-foreground hover:text-primary gap-1"
                      >
                        <RefreshCw className={`h-3 w-3 ${isReanalyzing ? "animate-spin" : ""}`} />
                        {isReanalyzing ? "Analyzing..." : "Re-analyze"}
                      </Button>
                    </CardHeader>
                    <CardContent>
                      <p className="text-foreground leading-relaxed text-lg">{currentInsight.summary}</p>
                    </CardContent>
                  </Card>

                  {/* Budget Health Score - 디자인 유지 */}
                  <Card className="mb-6 animate-in fade-in slide-in-from-bottom-4 duration-500 delay-100">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <Target className="h-5 w-5 text-accent" />
                        Budget Health Score
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="flex items-center justify-between mb-4">
                        <div>
                          <p className="text-3xl font-bold text-foreground">{currentInsight.budgetHealth.score}/100</p>
                          <p className="text-muted-foreground">{currentInsight.budgetHealth.status}</p>
                        </div>
                        <div className="text-right">
                          <Badge variant="secondary" className="mb-2">
                            {currentInsight.budgetHealth.status}
                          </Badge>
                        </div>
                      </div>
                      <Progress value={currentInsight.budgetHealth.score} className="mb-3 h-3" />
                      <p className="text-sm text-muted-foreground">{currentInsight.budgetHealth.description}</p>
                    </CardContent>
                  </Card>
                </>
              ) : (
                /* 데이터는 있는데 AI 분석만 없는 경우 (예: API 실패, 혹은 분석 전) */
<Card className="mb-6 border-dashed border-2 bg-slate-50/50">
                <CardContent className="flex flex-col items-center justify-center py-12 text-center">
                  <div className="bg-purple-100 p-4 rounded-full mb-4">
                    <Sparkles className="h-8 w-8 text-purple-600" />
                  </div>
                  <h3 className="text-xl font-semibold mb-2">Ready to Analyze?</h3>
                  <p className="text-muted-foreground mb-6 max-w-md">
                    Get personalized insights, budget health scores, and spending recommendations powered by AI.
                  </p>
                  <Button 
                    size="lg" 
                    onClick={handleReanalyze} 
                    disabled={isReanalyzing}
                    className="gap-2 bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white"
                  >
                    {isReanalyzing ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Analyzing...
                      </>
                    ) : (
                      <>
                        <Brain className="h-4 w-4" />
                        Generate AI Insights
                      </>
                    )}
                  </Button>
                </CardContent>
              </Card>
              )}

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
                {/* Spending Trends (AI Data) - 디자인 유지 */}
                {currentInsight && (
                  <Card className="animate-in fade-in slide-in-from-bottom-4 duration-500 delay-200 h-full">
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <TrendingUp className="h-5 w-5 text-accent" />
                        Spending Trends
                      </CardTitle>
                      <CardDescription>Month-over-month changes by category</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-4">
                        {currentInsight.trends.map((trend, index) => (
                          <div key={index} className="flex items-start gap-3 p-3 border border-border rounded-lg hover:bg-slate-50 transition-colors">
                            {getTrendIcon(trend.type)}
                            <div className="flex-1">
                              <div className="flex items-center justify-between mb-1">
                                <p className="font-medium">{trend.category}</p>
                                <Badge variant="outline">{trend.change}</Badge>
                              </div>
                              <p className="text-sm text-muted-foreground">{trend.description}</p>
                            </div>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                )}

                {/* Category Breakdown - 디자인 유지 */}
                <Card className={`h-full ${currentInsight ? "animate-in fade-in slide-in-from-bottom-4 duration-500 delay-300" : ""}`}>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <PieChart className="h-5 w-5 text-accent" />
                      Category Breakdown
                    </CardTitle>
                    <CardDescription>
                      Spending distribution for{" "}
                      {new Date(selectedMonth + "-01").toLocaleDateString("en-US", { year: "numeric", month: "long" })}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      {categoryData.map((category, index) => (
                        <div key={category.category} className="flex items-center justify-between p-2 rounded hover:bg-slate-50">
                          <div className="flex items-center gap-3">
                            <div
                              className="w-3 h-3 rounded-full shadow-sm"
                              style={{ backgroundColor: COLORS[index % COLORS.length] }}
                            />
                            <span className="text-sm font-medium">{category.category}</span>
                          </div>
                          <div className="text-right">
                            <p className="font-bold text-gray-900">{formatCurrency(category.amount)}</p>
                            <p className="text-xs text-muted-foreground">{category.percentage}%</p>
                          </div>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* AI Recommendations - 디자인 유지 */}
              {currentInsight && (
                <Card className="mb-6 animate-in fade-in slide-in-from-bottom-4 duration-500 delay-400">
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <AlertCircle className="h-5 w-5 text-accent" />
                      AI Recommendations
                    </CardTitle>
                    <CardDescription>Personalized suggestions to improve your financial health</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      {currentInsight.recommendations.map((rec, index) => (
                        <div key={index} className="p-4 border border-border rounded-lg bg-white shadow-sm">
                          <div className="flex items-start justify-between mb-2">
                            <h4 className="font-bold text-lg">{rec.title}</h4>
                            <Badge className={`${getPriorityColor(rec.priority)} border text-xs px-2 py-1`}>
                              {rec.priority} priority
                            </Badge>
                          </div>
                          <p className="text-sm text-gray-600 leading-relaxed">{rec.description}</p>
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Monthly Spending Chart - 디자인 유지 */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <BarChart3 className="h-5 w-5 text-accent" />
                    Monthly Overview
                  </CardTitle>
                  <CardDescription>
                    Total spent: <span className="font-bold text-primary">{formatCurrency(currentMonthData.total)}</span> • {currentMonthData.transactions} transactions
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="h-64">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={categoryData}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} />
                        <XAxis dataKey="category" tickLine={false} axisLine={false} fontSize={12} />
                        <YAxis 
                          tickFormatter={(value) => `₩${value.toLocaleString()}`} 
                          fontSize={12} 
                          tickLine={false}
                          axisLine={false}
                        />
                        <Tooltip
                          cursor={{fill: 'transparent'}}
                          contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                          formatter={(value) => [formatCurrency(Number(value)), "Amount"]}
                        />
                        <Bar dataKey="amount" fill="#8b5cf6" radius={[4, 4, 0, 0]} barSize={40} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </CardContent>
              </Card>
            </>
          ) : (
            /* 데이터가 없을 때 */
            <div className="flex flex-col items-center justify-center py-20 bg-slate-50 rounded-xl border-2 border-dashed border-slate-200">
              <Brain className="h-16 w-16 text-slate-300 mb-4" />
              <h3 className="text-xl font-semibold text-slate-600">No Data Available</h3>
              <p className="text-slate-500">There are no transactions recorded for this month.</p>
            </div>
          )
        )}
      </div>
    </div>
  )
}