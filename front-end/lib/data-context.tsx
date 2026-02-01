"use client"

import type React from "react"
import { createContext, useContext, useState, useEffect } from "react"
import { mockTransactions, type PaymentTransaction } from "./mock-data"
import { useRouter, usePathname } from "next/navigation" 
import { api } from "@/lib/api"

interface DataContextType {
  transactions: PaymentTransaction[]
  categories: string[]
  addTransaction: (transaction: Omit<PaymentTransaction, "id">) => void
  updateTransaction: (id: string, updates: Partial<PaymentTransaction>) => void
  deleteTransaction: (id: string) => void
  addCategory: (category: string) => void
  deleteCategory: (category: string) => void
  importTransactions: (transactions: PaymentTransaction[]) => void
  exportTransactions: () => string
  clearAllData: () => void
}

const DataContext = createContext<DataContextType | undefined>(undefined)

export function DataProvider({ children }: { children: React.ReactNode }) {
  const [transactions, setTransactions] = useState<PaymentTransaction[]>([])
  const [categories, setCategories] = useState<string[]>([])

  const [isAuthChecked, setIsAuthChecked] = useState(false)
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    console.log("Check user login")
    const checkLogin = async () => {
      if(isAuthChecked) return;
      console.log(pathname)
      if (pathname.startsWith("/login") || pathname.startsWith("/oauth2")) {
        return
      }

      try {
        console.log("DataProvider: Checking login status...")
        await api.get("/api/user/me")
        
        console.log("DataProvider: Login verified.")
        setIsAuthChecked(true)
      } catch (error) {
        console.error("DataProvider: Login check failed, redirecting...", error)
        router.push("/login/google")
      }
    }

    checkLogin()
  }, [pathname, router])
  useEffect(() => {
    const accessToken = localStorage.getItem("accessToken"); 

    if (!accessToken) {
        setTransactions([]); 
        return; 
    }
    if(transactions.length != 0)
    {
      return;
    }
    const fetchInitialData = async () => {
        try {
            const transactionsData = await api.get('/api/transactions'); 
            
            setTransactions(transactionsData);
            extractCategoriesFromTransactions(transactionsData);
            
        } catch (error) {
            console.error("데이터 불러오기 실패:", error);
            setTransactions([]); 
        }
    };
    // 함수 실행
    fetchInitialData();
    
  }, [])

  const extractCategoriesFromTransactions = (transactionList: PaymentTransaction[]) => {
    const uniqueCategories = Array.from(new Set(transactionList.map((t) => t.category))).sort()
    setCategories(uniqueCategories)
  }


  const addTransaction = (transaction: Omit<PaymentTransaction, "id">) => {
    const newTransaction: PaymentTransaction = {
      ...transaction,
      id: Date.now().toString() + Math.random().toString(36).substr(2, 9),
    }
    setTransactions((prev) => [newTransaction, ...prev])

    if (!categories.includes(transaction.category)) {
      setCategories((prev) => [...prev, transaction.category].sort())
    }
  }
  const updateTransaction = (id: string, updates: Partial<PaymentTransaction>) => {
  setTransactions((prev) =>
    prev.map((transaction) => (transaction.id === id ? { ...transaction, ...updates } : transaction)),
  )

  // 1. 변수에 category 값을 먼저 할당합니다.
  const newCategory = updates.category;

  // 2. 해당 변수가 string 타입인지 명확하게 확인합니다.
  if (newCategory && typeof newCategory === 'string' && !categories.includes(newCategory)) {
    // if 블록 안에서 newCategory는 이제 확실한 string 타입입니다.
    setCategories((prev) => [...prev, newCategory].sort())
  }
}


  const deleteTransaction = (id: string) => {
    setTransactions((prev) => prev.filter((transaction) => transaction.id !== id))
  }

  const addCategory = (category: string) => {
    if (!categories.includes(category)) {
      setCategories((prev) => [...prev, category].sort())
    }
  }

  const deleteCategory = (category: string) => {
    setCategories((prev) => prev.filter((cat) => cat !== category))
    setTransactions((prev) =>
      prev.map((transaction) =>
        transaction.category === category ? { ...transaction, category: "Uncategorized" } : transaction,
      ),
    )
  }

  const importTransactions = (newTransactions: PaymentTransaction[]) => {
    setTransactions(newTransactions)
    extractCategoriesFromTransactions(newTransactions)
  }

  const exportTransactions = () => {
    return JSON.stringify(transactions, null, 2)
  }

  const clearAllData = () => {
    setTransactions([])
    setCategories([])
  }
  if (!isAuthChecked) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-lg">Checking login status...</div>
      </div>
    )
  }
  return (
    <DataContext.Provider
      value={{
        transactions,
        categories,
        addTransaction,
        updateTransaction,
        deleteTransaction,
        addCategory,
        deleteCategory,
        importTransactions,
        exportTransactions,
        clearAllData,
      }}
    >
      {children}
    </DataContext.Provider>
  )
}

export function useData() {
  const context = useContext(DataContext)
  if (context === undefined) {
    throw new Error("useData must be used within a DataProvider")
  }
  return context
}
