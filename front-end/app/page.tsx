"use client";

import Link from "next/link";
import { motion, Variants } from "framer-motion";
import { 
  CreditCard, 
  BookOpen, 
  Server, 
  Activity, 
  ArrowRight, 
  Github,
  Terminal
} from "lucide-react";

// 애니메이션 설정 (과하지 않고 부드럽게)
const containerVariants: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1
    }
  }
};

const itemVariants: Variants = {
  hidden: { y: 20, opacity: 0 },
  visible: {
    y: 0,
    opacity: 1,
    transition: { type: "spring", stiffness: 100 }
  }
};

export default function Dashboard() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 p-8 font-sans">
      <div className="max-w-6xl mx-auto">
        
        {/* 헤더 섹션 */}
        <header className="mb-12">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
          >
            <h1 className="text-4xl font-bold mb-2 text-slate-800">Hello, Suhyun.</h1>
            <p className="text-slate-500 text-lg">
              LifeHub Control Center에 오신 것을 환영합니다.
            </p>
          </motion.div>
        </header>

        {/* 메인 그리드 (Bento Grid Layout) */}
        <motion.div 
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          variants={containerVariants}
          initial="hidden"
          animate="visible"
        >
          
          {/* 1. 가계부 위젯 (가장 중요) */}
          <Link href="/card" className="col-span-1 md:col-span-2 group">
            <motion.div 
              variants={itemVariants}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              className="h-full bg-white p-6 rounded-2xl shadow-sm border border-slate-200 hover:shadow-md transition-all cursor-pointer relative overflow-hidden"
            >
              <div className="absolute top-0 right-0 p-6 opacity-10 group-hover:opacity-20 transition-opacity">
                <CreditCard size={120} />
              </div>
              
              <div className="flex justify-between items-start mb-4">
                <div className="p-3 bg-blue-100 rounded-xl text-blue-600">
                  <CreditCard size={24} />
                </div>
                <span className="flex items-center text-sm font-medium text-slate-400 group-hover:text-blue-600 transition-colors">
                  Open App <ArrowRight size={16} className="ml-1" />
                </span>
              </div>
              
              <h2 className="text-2xl font-bold mb-2">Finance Manager</h2>
              <p className="text-slate-500 mb-6">
                카드 소비 내역 분석 및 지출 관리
              </p>

              {/* 가짜 데이터 (나중에 API 연동 가능) */}
              <div className="flex gap-4">
                <div className="bg-slate-50 px-4 py-2 rounded-lg border border-slate-100">
                  <span className="text-xs text-slate-400 block">이번 달 지출</span>
                  <span className="font-semibold text-slate-700">₩ 450,200</span>
                </div>
                <div className="bg-slate-50 px-4 py-2 rounded-lg border border-slate-100">
                  <span className="text-xs text-slate-400 block">상태</span>
                  <span className="font-semibold text-green-600 flex items-center gap-1">
                    <Activity size={12} /> Stable
                  </span>
                </div>
              </div>
            </motion.div>
          </Link>

          {/* 2. 서버 상태 위젯 (엔지니어 감성) */}
          <motion.div 
            variants={itemVariants}
            className="bg-slate-900 text-white p-6 rounded-2xl shadow-sm hover:shadow-lg transition-shadow"
          >
            <div className="flex justify-between items-start mb-6">
              <div className="p-3 bg-slate-800 rounded-xl text-green-400">
                <Server size={24} />
              </div>
              <div className="flex items-center gap-2">
                <span className="relative flex h-3 w-3">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
                </span>
                <span className="text-xs font-mono text-green-400">ONLINE</span>
              </div>
            </div>
            
            <h3 className="text-lg font-bold mb-1">Server Status</h3>
            <div className="font-mono text-sm text-slate-400 space-y-2 mt-4">
              <div className="flex justify-between">
                <span>CPU Load</span>
                <span>12%</span>
              </div>
              <div className="flex justify-between">
                <span>Memory</span>
                <span>4.2GB / 8GB</span>
              </div>
              <div className="flex justify-between">
                <span>Uptime</span>
                <span>3d 12h 40m</span>
              </div>
            </div>
          </motion.div>

          {/* 3. 독서록 위젯 (준비 중) */}
          <Link href="/book" className="col-span-1 group">
             <motion.div 
              variants={itemVariants}
              whileHover={{ y: -5 }}
              className="h-full bg-white p-6 rounded-2xl shadow-sm border border-slate-200 hover:border-orange-200 transition-colors cursor-pointer"
            >
              <div className="p-3 bg-orange-100 rounded-xl text-orange-600 w-fit mb-4">
                <BookOpen size={24} />
              </div>
              <h3 className="text-xl font-bold mb-2">Reading Log</h3>
              <p className="text-slate-500 text-sm mb-4">
                읽은 책 기록 및 독후감 작성
              </p>
              <div className="text-xs font-medium px-2 py-1 bg-slate-100 rounded text-slate-500 w-fit">
                Coming Soon
              </div>
            </motion.div>
          </Link>

          {/* 4. 개발자 빠른 링크 */}
          <motion.div 
            variants={itemVariants}
            className="col-span-1 md:col-span-2 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl p-6 text-white flex flex-col justify-center relative overflow-hidden"
          >
            <div className="absolute right-0 bottom-0 opacity-10 transform translate-x-4 translate-y-4">
              <Terminal size={150} />
            </div>
            
            <h3 className="text-2xl font-bold mb-2 z-10">Dev Environment</h3>
            <p className="text-indigo-100 mb-6 max-w-md z-10">
              Raspberry Pi 5 + Spring Boot + Next.js
              <br/>
              Self-Hosted CI/CD Pipeline Active
            </p>
            
            <div className="flex gap-3 z-10">
              <a href="https://github.com/suhyun444" target="_blank" className="flex items-center gap-2 px-4 py-2 bg-white/10 backdrop-blur-md rounded-lg hover:bg-white/20 transition-colors text-sm font-medium">
                <Github size={16} /> GitHub
              </a>
              <div className="flex items-center gap-2 px-4 py-2 bg-white/10 backdrop-blur-md rounded-lg text-sm font-medium">
                <Terminal size={16} /> SSH Connect
              </div>
            </div>
          </motion.div>

        </motion.div>
      </div>
    </div>
  );
}