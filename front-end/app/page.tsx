"use client";

import { useState } from "react";
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { 
  Plus, X, CreditCard, Server, Clock, 
  Activity, Zap, LayoutGrid, Terminal, Github
} from "lucide-react";

// --- 1. 타입 및 설정 정의 ---
// icon: 1x1 (앱 아이콘)
// small: 2x1 (가로형 버튼)
// medium: 2x2 (정사각형 위젯)
// wide: 4x2 (와이드 패널)
type WidgetSize = "icon" | "small" | "medium" | "wide"; 
type WidgetType = "finance" | "server" | "clock" | "link";

interface WidgetItem {
  id: string;
  type: WidgetType;
  size: WidgetSize;
  data?: any; // 추가 데이터 (링크 주소 등)
}

// --- 2. 위젯 렌더링 컴포넌트 ---
const WidgetContent = ({ type, size }: { type: WidgetType, size: WidgetSize }) => {
  // 1x1 아이콘 모드일 때
  if (size === "icon") {
    switch (type) {
      case "finance": return <div className="flex justify-center items-center h-full bg-blue-500 text-white"><CreditCard size={24}/></div>;
      case "server": return <div className="flex justify-center items-center h-full bg-slate-800 text-green-400"><Terminal size={24}/></div>;
      case "clock": return <div className="flex justify-center items-center h-full bg-indigo-500 text-white"><Clock size={24}/></div>;
      case "link": return <div className="flex justify-center items-center h-full bg-white text-slate-800 border-2 border-slate-100"><Github size={28}/></div>;
    }
  }

  // 그 외 사이즈일 때 (상세 정보 표시)
  switch (type) {
    case "finance":
      return (
        <Link href="/card" className="flex flex-col h-full justify-between p-4 bg-white text-slate-800 hover:bg-slate-50 transition-colors">
          <div className="flex justify-between items-start">
            <div className="p-1.5 bg-blue-100 text-blue-600 rounded-md"><CreditCard size={18}/></div>
            {size !== "small" && <span className="text-[10px] font-bold text-slate-400 px-2 py-1 bg-slate-100 rounded-full">VISA</span>}
          </div>
          <div>
            <div className="text-xl font-bold tracking-tight">₩ 450,200</div>
            <div className="text-[10px] text-slate-500">이번 달 지출</div>
          </div>
        </Link>
      );
    case "server":
      return (
        <div className="flex flex-col h-full justify-between p-4 bg-slate-900 text-white relative overflow-hidden">
          <div className="flex items-center gap-2 text-green-400 z-10">
            <Activity size={16} />
            <span className="text-[10px] font-bold uppercase tracking-wider">System</span>
          </div>
          
          <div className="space-y-2 z-10">
             <div className="flex justify-between text-[10px] text-slate-400">
               <span>CPU Load</span> <span className="text-white">12%</span>
             </div>
             <div className="w-full bg-slate-700 h-1 rounded-full"><div className="bg-green-500 w-[12%] h-full rounded-full"></div></div>
             
             {/* 큰 위젯일 때만 RAM 정보 표시 */}
             {size !== "small" && (
               <>
                 <div className="flex justify-between text-[10px] text-slate-400 mt-1">
                   <span>Memory</span> <span className="text-white">4.2G</span>
                 </div>
                 <div className="w-full bg-slate-700 h-1 rounded-full"><div className="bg-purple-500 w-[60%] h-full rounded-full"></div></div>
               </>
             )}
          </div>
          {/* 배경 장식 */}
          <div className="absolute -right-4 -bottom-4 text-slate-800 opacity-50"><Server size={80} /></div>
        </div>
      );
    case "clock":
      return (
        <div className="flex flex-col h-full justify-center items-center bg-indigo-600 text-white p-3 relative overflow-hidden">
          <div className="absolute top-2 left-3 text-[10px] opacity-60">SEOUL</div>
          <div className="text-3xl font-bold font-mono tracking-tighter">19:34</div>
          <div className="text-[10px] opacity-80 mt-1 font-medium bg-white/20 px-2 py-0.5 rounded-full">PM</div>
        </div>
      );
    case "link":
      return (
        <Link href="https://github.com/suhyun444" target="_blank" className="flex flex-col h-full justify-center items-center bg-slate-950 text-white p-4 group hover:bg-black transition-colors">
          <Github size={32} className="group-hover:scale-110 transition-transform"/>
          <span className="text-xs font-bold mt-2">GitHub</span>
        </Link>
      );
    default:
      return null;
  }
};

export default function Dashboard() {
  // 초기 상태: 다양한 크기의 위젯 배치
  const [widgets, setWidgets] = useState<WidgetItem[]>([
    { id: "1", type: "server", size: "medium" }, // 2x2
    { id: "2", type: "finance", size: "wide" },   // 4x2
    { id: "3", type: "clock", size: "small" },    // 2x1
    { id: "4", type: "link", size: "icon" },      // 1x1
    { id: "5", type: "finance", size: "icon" },   // 1x1
  ]);

  const [isModalOpen, setIsModalOpen] = useState(false);

  // 위젯 추가
  const addWidget = (type: WidgetType, size: WidgetSize) => {
    const newWidget: WidgetItem = {
      id: Math.random().toString(36).substr(2, 9),
      type,
      size,
    };
    setWidgets([...widgets, newWidget]);
    setIsModalOpen(false);
  };

  // 위젯 삭제
  const removeWidget = (id: string) => {
    setWidgets(widgets.filter((w) => w.id !== id));
  };

  // ★ 그리드 클래스 매핑 (핵심 로직)
  // 6열 그리드 기준: col-span-1 = 1칸, col-span-2 = 2칸...
  const getSizeClass = (size: WidgetSize) => {
    switch (size) {
      case "icon":   return "col-span-1 row-span-1"; // 1x1
      case "small":  return "col-span-2 row-span-1"; // 2x1
      case "medium": return "col-span-2 row-span-2"; // 2x2
      case "wide":   return "col-span-4 row-span-2"; // 4x2
      default: return "col-span-1 row-span-1";
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 p-6 md:p-10 font-sans text-slate-900 select-none">
      <div className="max-w-6xl mx-auto">
        
        <header className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-3xl font-bold text-slate-800 tracking-tight">LifeHub OS</h1>
            <p className="text-slate-500 text-sm">Design your own workflow.</p>
          </div>
          <button 
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2 bg-slate-900 text-white rounded-full hover:bg-slate-800 transition-all shadow-lg shadow-slate-300/50 active:scale-95"
          >
            <Plus size={18} /> <span className="text-sm font-bold">Add Widget</span>
          </button>
        </header>

        {/* ★ High Density Grid Layout 
          모바일: 3열 / PC: 6열
          높이: 100px로 고정 (촘촘함)
          gap: 12px (깔끔함)
        */}
        <div className="grid grid-cols-3 md:grid-cols-6 auto-rows-[100px] gap-3 grid-flow-dense">
          
          <AnimatePresence>
            {widgets.map((widget) => (
              <motion.div
                key={widget.id}
                layoutId={widget.id}
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.5 }}
                transition={{ type: "spring", stiffness: 400, damping: 30 }}
                className={`relative group rounded-2xl overflow-hidden shadow-sm hover:shadow-xl transition-shadow border border-slate-200/50 ${getSizeClass(widget.size)}`}
              >
                {/* 삭제 버튼 (우측 상단, 호버 시 노출) */}
                <button
                  onClick={() => removeWidget(widget.id)}
                  className="absolute top-2 right-2 z-20 p-1 bg-black/50 text-white rounded-full opacity-0 group-hover:opacity-100 transition-all hover:bg-red-500 hover:scale-110"
                >
                  <X size={12} />
                </button>
                
                {/* 위젯 렌더링 */}
                <WidgetContent type={widget.type} size={widget.size} />
              </motion.div>
            ))}
          </AnimatePresence>

          {/* 빈 공간 추가 버튼 */}
          <motion.button
            layout
            onClick={() => setIsModalOpen(true)}
            className="col-span-1 row-span-1 border-2 border-dashed border-slate-300 rounded-2xl flex flex-col items-center justify-center text-slate-400 hover:text-slate-600 hover:border-slate-400 hover:bg-slate-200/30 transition-all"
          >
            <Plus size={24} />
          </motion.button>
        </div>

      </div>

      {/* --- 위젯 추가 모달 --- */}
      <AnimatePresence>
        {isModalOpen && (
          <motion.div 
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/60 backdrop-blur-md z-50 flex items-center justify-center p-4"
            onClick={() => setIsModalOpen(false)}
          >
            <motion.div 
              initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.9, opacity: 0 }}
              className="bg-slate-50 rounded-3xl p-6 w-full max-w-2xl shadow-2xl max-h-[80vh] overflow-y-auto"
              onClick={e => e.stopPropagation()}
            >
              <div className="flex justify-between items-center mb-6">
                <div className="flex items-center gap-2">
                  <LayoutGrid className="text-slate-400"/>
                  <h2 className="text-xl font-bold text-slate-800">Widget Gallery</h2>
                </div>
                <button onClick={() => setIsModalOpen(false)} className="p-2 hover:bg-slate-200 rounded-full"><X size={20}/></button>
              </div>

              <div className="space-y-8">
                {/* 섹션 1: 시스템 */}
                <section>
                  <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3 ml-1">System & Monitoring</h3>
                  <div className="flex gap-3 overflow-x-auto pb-2">
                    <button onClick={() => addWidget("server", "icon")} className="flex-none w-20 h-20 bg-slate-800 rounded-xl flex items-center justify-center text-green-400 hover:scale-105 transition-transform"><Terminal size={24}/></button>
                    <button onClick={() => addWidget("server", "medium")} className="flex-none w-40 h-40 bg-slate-900 rounded-xl p-4 text-left text-white hover:scale-105 transition-transform shadow-lg">
                      <Activity size={20} className="mb-2 text-green-400"/>
                      <div className="text-xs text-slate-400">Server Status</div>
                      <div className="font-bold">Online</div>
                    </button>
                  </div>
                </section>

                {/* 섹션 2: 금융 */}
                <section>
                  <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3 ml-1">Finance</h3>
                  <div className="flex gap-3 overflow-x-auto pb-2">
                    <button onClick={() => addWidget("finance", "icon")} className="flex-none w-20 h-20 bg-blue-500 rounded-xl flex items-center justify-center text-white hover:scale-105 transition-transform shadow-lg shadow-blue-500/30"><CreditCard size={24}/></button>
                    <button onClick={() => addWidget("finance", "small")} className="flex-none w-40 h-20 bg-white border border-slate-200 rounded-xl p-3 text-left hover:scale-105 transition-transform">
                      <div className="text-[10px] text-slate-400">Total</div>
                      <div className="font-bold text-slate-800">₩ 450k</div>
                    </button>
                    <button onClick={() => addWidget("finance", "wide")} className="flex-none w-80 h-40 bg-white border border-slate-200 rounded-xl p-4 text-left hover:scale-105 transition-transform shadow-sm">
                      <div className="flex justify-between mb-8"><CreditCard className="text-blue-500"/><span className="text-xs bg-slate-100 px-2 py-1 rounded">Analysis</span></div>
                      <div className="text-2xl font-bold">₩ 450,200</div>
                    </button>
                  </div>
                </section>

                {/* 섹션 3: 유틸리티 */}
                <section>
                  <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3 ml-1">Utilities</h3>
                  <div className="flex gap-3 overflow-x-auto pb-2">
                    <button onClick={() => addWidget("clock", "small")} className="flex-none w-40 h-20 bg-indigo-500 text-white rounded-xl flex items-center justify-center font-mono font-bold text-xl hover:scale-105 transition-transform shadow-lg shadow-indigo-500/30">19:35</button>
                    <button onClick={() => addWidget("link", "icon")} className="flex-none w-20 h-20 bg-black text-white rounded-xl flex items-center justify-center hover:scale-105 transition-transform"><Github size={24}/></button>
                  </div>
                </section>
              </div>

            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}