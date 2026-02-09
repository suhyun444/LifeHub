"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import { 
  Plus, 
  CreditCard, 
  Server, 
  BookOpen, 
  X, 
  Cpu, 
  Clock, 
  Trash2
} from "lucide-react";

// --- 1. 위젯 종류 정의 ---
type WidgetType = "finance" | "server" | "book" | "clock" | null;

interface WidgetOption {
  type: WidgetType;
  label: string;
  icon: any;
  desc: string;
  color: string;
}

const WIDGET_OPTIONS: WidgetOption[] = [
  { 
    type: "finance", 
    label: "Finance Manager", 
    icon: CreditCard, 
    desc: "카드 소비 내역 및 지출 분석", 
    color: "bg-blue-500" 
  },
  { 
    type: "server", 
    label: "Server Status", 
    icon: Server, 
    desc: "Nginx 및 시스템 리소스 모니터링", 
    color: "bg-green-500" 
  },
  { 
    type: "book", 
    label: "Reading Log", 
    icon: BookOpen, 
    desc: "독서 기록 및 서평 작성", 
    color: "bg-orange-500" 
  },
  { 
    type: "clock", 
    label: "Digital Clock", 
    icon: Clock, 
    desc: "현재 시간 및 날짜 표시", 
    color: "bg-indigo-500" 
  },
];

// --- 2. 개별 위젯 컴포넌트들 ---
const FinanceWidget = () => (
  <Link href="/card" className="flex flex-col h-full justify-between">
    <div className="flex items-center gap-2 text-blue-600">
      <CreditCard size={20} />
      <span className="font-bold">Finance</span>
    </div>
    <div>
      <div className="text-2xl font-bold text-slate-800">₩ 450,200</div>
      <div className="text-xs text-slate-500">이번 달 지출</div>
    </div>
    <div className="text-xs text-blue-500 bg-blue-50 px-2 py-1 rounded w-fit">
      분석 보러가기 →
    </div>
  </Link>
);

const ServerWidget = () => (
  <div className="flex flex-col h-full justify-between">
    <div className="flex items-center gap-2 text-green-600">
      <Server size={20} />
      <span className="font-bold">System</span>
    </div>
    <div className="space-y-1">
      <div className="flex justify-between text-xs text-slate-600">
        <span>CPU</span>
        <span className="font-mono">12%</span>
      </div>
      <div className="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden">
        <div className="bg-green-500 h-full w-[12%]"></div>
      </div>
      <div className="flex justify-between text-xs text-slate-600 mt-1">
        <span>RAM</span>
        <span className="font-mono">4.2G</span>
      </div>
      <div className="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden">
        <div className="bg-green-500 h-full w-[50%]"></div>
      </div>
    </div>
    <div className="flex items-center gap-1 text-[10px] text-green-600 font-medium">
      <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
      Online
    </div>
  </div>
);

const ClockWidget = () => {
  const [time, setTime] = useState(new Date());
  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);
  
  return (
    <div className="flex flex-col h-full justify-center items-center text-slate-700">
      <div className="text-3xl font-bold font-mono">
        {time.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' })}
      </div>
      <div className="text-xs text-slate-400 mt-1">
        {time.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric', weekday: 'short' })}
      </div>
    </div>
  );
};

// --- 3. 메인 페이지 ---
export default function Dashboard() {
  // 3x3 그리드 (총 9칸), 초기값은 모두 null (빈 칸)
  const [gridState, setGridState] = useState<WidgetType[]>(Array(9).fill(null));
  
  // 모달 상태 (어떤 인덱스를 클릭했는지)
  const [selectedSlot, setSelectedSlot] = useState<number | null>(null);

  // 위젯 추가 함수
  const addWidget = (type: WidgetType) => {
    if (selectedSlot !== null) {
      const newGrid = [...gridState];
      newGrid[selectedSlot] = type;
      setGridState(newGrid);
      setSelectedSlot(null); // 모달 닫기
    }
  };

  // 위젯 삭제 함수
  const removeWidget = (index: number, e: React.MouseEvent) => {
    e.stopPropagation(); // 부모 클릭 이벤트 방지
    const newGrid = [...gridState];
    newGrid[index] = null;
    setGridState(newGrid);
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 p-8 font-sans">
      <div className="max-w-4xl mx-auto">
        
        {/* 헤더 */}
        <header className="mb-8 flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-slate-800">My Dashboard</h1>
            <p className="text-slate-500">원하는 기능을 클릭해서 추가하세요.</p>
          </div>
          <div className="p-2 bg-white rounded-full shadow-sm border border-slate-200">
            <Cpu size={20} className="text-slate-400" />
          </div>
        </header>

        {/* --- 메인 그리드 영역 (3x3) --- */}
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4 auto-rows-[160px]">
          {gridState.map((widgetType, index) => (
            <motion.div
              key={index}
              layout
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.2 }}
              className={`relative rounded-2xl border transition-all duration-300 group
                ${widgetType 
                  ? "bg-white border-slate-200 shadow-sm hover:shadow-md" 
                  : "bg-slate-100/50 border-dashed border-slate-300 hover:border-slate-400 hover:bg-slate-100 cursor-pointer flex justify-center items-center"
                }`}
              onClick={() => !widgetType && setSelectedSlot(index)}
            >
              {/* 위젯이 있을 때 */}
              {widgetType ? (
                <div className="h-full p-5 relative">
                  {/* 삭제 버튼 (호버 시 등장) */}
                  <button 
                    onClick={(e) => removeWidget(index, e)}
                    className="absolute top-2 right-2 p-1.5 bg-slate-100 text-slate-400 rounded-full opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-50 hover:text-red-500"
                  >
                    <X size={14} />
                  </button>
                  
                  {/* 위젯 렌더링 */}
                  {widgetType === "finance" && <FinanceWidget />}
                  {widgetType === "server" && <ServerWidget />}
                  {widgetType === "clock" && <ClockWidget />}
                  {widgetType === "book" && (
                    <div className="flex flex-col items-center justify-center h-full text-slate-400">
                       <BookOpen size={24} className="mb-2"/>
                       <span className="text-xs">준비 중</span>
                    </div>
                  )}
                </div>
              ) : (
                /* 위젯이 없을 때 (빈 슬롯) */
                <div className="text-slate-300 group-hover:text-slate-500 transition-colors">
                  <Plus size={32} />
                </div>
              )}
            </motion.div>
          ))}
        </div>

        {/* --- 위젯 선택 모달 (Popup) --- */}
        <AnimatePresence>
          {selectedSlot !== null && (
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4"
              onClick={() => setSelectedSlot(null)}
            >
              <motion.div 
                initial={{ y: 20, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                exit={{ y: 20, opacity: 0 }}
                className="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-lg"
                onClick={(e) => e.stopPropagation()} // 모달 내부 클릭 시 닫힘 방지
              >
                <div className="flex justify-between items-center mb-6">
                  <h3 className="text-lg font-bold">위젯 선택</h3>
                  <button onClick={() => setSelectedSlot(null)} className="text-slate-400 hover:text-slate-600">
                    <X size={20} />
                  </button>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  {WIDGET_OPTIONS.map((option) => (
                    <button
                      key={option.type}
                      onClick={() => addWidget(option.type)}
                      className="flex items-center gap-3 p-4 rounded-xl border border-slate-100 hover:border-slate-300 hover:bg-slate-50 transition-all text-left group"
                    >
                      <div className={`p-3 rounded-lg text-white ${option.color} shadow-sm group-hover:scale-110 transition-transform`}>
                        <option.icon size={20} />
                      </div>
                      <div>
                        <div className="font-bold text-sm text-slate-700">{option.label}</div>
                        <div className="text-xs text-slate-400">{option.desc}</div>
                      </div>
                    </button>
                  ))}
                </div>
              </motion.div>
            </motion.div>
          )}
        </AnimatePresence>

      </div>
    </div>
  );
}