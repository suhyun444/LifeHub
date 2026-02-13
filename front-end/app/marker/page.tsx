"use client";

import { useState } from "react";
import { useRouter } from "next/navigation"; // 페이지 이동용
import { Plus, BookMarked, Trash2, MapPin } from "lucide-react";
import { motion } from "framer-motion";
import { useMarkers, Marker } from "@/lib/marker-context"; // Marker 타입 import 필요

// ★ dnd-kit 라이브러리
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from "@dnd-kit/core";
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  rectSortingStrategy,
  arrayMove,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

// ★ [분리된 컴포넌트] 드래그 가능한 마커 (Link 대신 div 사용)
function SortableMarker({ marker, onDelete }: { marker: Marker, onDelete: (id: string) => void }) {
  const router = useRouter();
  
  // ID를 무조건 문자열로 변환해서 전달 (중요!)
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: String(marker.id) });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 50 : "auto",
    opacity: isDragging ? 0.8 : 1,
  };

  // 상세 페이지 이동 함수
  const goToDetail = () => {
    if (isDragging) return; // 드래그 중이면 이동 X
    router.push(`/marker/game?id=${marker.id}&title=${encodeURIComponent(marker.title)}&color=${marker.color}`);
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onClick={goToDetail} // Link 대신 클릭 이벤트 사용
      className="touch-none cursor-pointer" // 터치 이벤트 방지 및 커서 설정
    >
      <motion.div
        whileHover={{ y: -5, scale: 1.02 }}
        animate={isDragging ? { scale: 1.05, y: 0 } : {}}
        className={`relative h-44 rounded-2xl p-6 flex flex-col justify-between shadow-xl group overflow-hidden select-none ${marker.color}`}
      >
        <MapPin className="absolute -right-4 -bottom-4 text-white opacity-20 rotate-12" size={100} />

        <div className="z-10">
          <h2 className="text-2xl font-bold truncate tracking-tight">{marker.title}</h2>
          <p className="text-white/60 text-xs mt-1 font-mono uppercase">ID: {String(marker.id).slice(-4)}</p>
        </div>

        <div className="z-10 flex justify-between items-center mt-4">
          <span className="text-[10px] font-bold bg-black/20 px-3 py-1.5 rounded-full backdrop-blur-md group-hover:bg-black/30 transition-colors">
            OPEN GAME
          </span>
          <button
            onClick={(e) => {
              e.stopPropagation(); // 부모 클릭(이동) 방지
              if (confirm("삭제하시겠습니까?")) onDelete(marker.id);
            }}
            onPointerDown={(e) => e.stopPropagation()} // 드래그 시작 방지
            className="p-2 text-white/50 hover:text-white hover:bg-white/20 rounded-full transition-all"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </motion.div>
    </div>
  );
}

// ★ 메인 페이지 컴포넌트
export default function Page() {
  const { markers, isLoading, createMarker, deleteMarker, moveMarker } = useMarkers();
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newTitle, setNewTitle] = useState("");

  // ★ 센서 설정: 8px 이상 움직여야 드래그로 인식
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    
    if (over && active.id !== over.id) {
      // ID를 문자열로 확실하게 변환해서 전달
      await moveMarker(String(active.id), String(over.id));
    }
  };

  const handleAdd = async () => {
    if (!newTitle) return;
    const colors = ["bg-rose-600", "bg-blue-600", "bg-emerald-600", "bg-violet-600", "bg-slate-700", "bg-amber-600"];
    const randomColor = colors[Math.floor(Math.random() * colors.length)];
    
    await createMarker(newTitle, randomColor);
    setNewTitle("");
    setIsModalOpen(false);
  };

  if (isLoading) return <div className="p-10 text-white">Loading...</div>;

  return (
    <div className="min-h-screen bg-slate-950 text-white p-8 font-sans">
      <div className="max-w-5xl mx-auto">
        <header className="flex justify-between items-end mb-10 border-b border-slate-800 pb-6">
          <div>
            <h1 className="text-4xl font-bold flex items-center gap-3 tracking-tight">
              <BookMarked className="text-rose-500" size={40}/> 
              My Markers
            </h1>
            <p className="text-slate-400 mt-2 text-sm">Game Guide & Quick Links Collection</p>
          </div>
          <button 
            onClick={() => setIsModalOpen(true)}
            className="px-5 py-2.5 bg-white text-slate-950 rounded-xl font-bold flex items-center gap-2 hover:bg-slate-200 transition-colors shadow-lg shadow-white/10"
          >
            <Plus size={18}/> New Marker
          </button>
        </header>

        {/* ★ DndContext 시작 */}
        <DndContext 
          sensors={sensors} 
          collisionDetection={closestCenter} 
          onDragEnd={handleDragEnd}
        >
          {/* items에 ID 문자열 배열 전달 */}
          <SortableContext items={markers.map(m => String(m.id))} strategy={rectSortingStrategy}>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-5">
              {markers.map((marker) => (
                <SortableMarker 
                  key={marker.id} 
                  marker={marker} 
                  onDelete={deleteMarker} 
                />
              ))}

              {markers.length === 0 && (
                <button 
                  onClick={() => setIsModalOpen(true)}
                  className="col-span-1 h-44 border-2 border-dashed border-slate-800 rounded-2xl flex flex-col items-center justify-center text-slate-600 hover:text-slate-400 hover:border-slate-600 hover:bg-slate-900 transition-all gap-3"
                >
                  <Plus size={32} />
                  <span className="font-bold">Create First Marker</span>
                </button>
              )}
            </div>
          </SortableContext>
        </DndContext>
      </div>

      {/* 모달 */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <motion.div 
            initial={{scale: 0.9, opacity: 0}} animate={{scale: 1, opacity: 1}}
            className="bg-slate-900 p-6 rounded-2xl w-full max-w-sm border border-slate-700 shadow-2xl"
          >
            <h3 className="text-lg font-bold mb-4 text-white">새 게임 마커 생성</h3>
            <input 
              autoFocus
              type="text" 
              placeholder="Game Name (e.g. Lost Ark)" 
              className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-white mb-4 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500 transition-all"
              value={newTitle}
              onChange={(e) => setNewTitle(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
            />
            <div className="flex gap-2 justify-end">
              <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-slate-400 hover:text-white text-sm">Cancel</button>
              <button onClick={handleAdd} className="px-5 py-2 bg-rose-600 rounded-lg font-bold hover:bg-rose-500 text-sm shadow-lg shadow-rose-600/20">Create</button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
}