"use client";

import { useState, useEffect, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { ArrowLeft, Plus, Trash2, BookOpen } from "lucide-react"; // 불필요한 아이콘 제거
import { motion } from "framer-motion";

// 카테고리 필드 제거
interface LinkItem {
  id: string;
  title: string;
  url: string;
}

function GameDetailContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const markerId = searchParams.get("id");
  const title = searchParams.get("title") || "Game Guide";
  const color = searchParams.get("color") || "bg-slate-800";

  const [links, setLinks] = useState<LinkItem[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  const [newTitle, setNewTitle] = useState("");
  const [newUrl, setNewUrl] = useState("");

  useEffect(() => {
    if (markerId) {
      const saved = localStorage.getItem(`marker-links-${markerId}`);
      if (saved) setLinks(JSON.parse(saved));
    }
  }, [markerId]);

  const addLink = () => {
    if (!newTitle || !newUrl || !markerId) return;

    let finalUrl = newUrl;
    if (!finalUrl.startsWith('http')) finalUrl = 'https://' + finalUrl;

    const newLink: LinkItem = {
      id: Date.now().toString(),
      title: newTitle,
      url: finalUrl,
    };

    const updated = [...links, newLink];
    setLinks(updated);
    localStorage.setItem(`marker-links-${markerId}`, JSON.stringify(updated));
    
    setNewTitle("");
    setNewUrl("");
    setIsModalOpen(false);
  };

  const deleteLink = (id: string) => {
    const updated = links.filter(l => l.id !== id);
    setLinks(updated);
    if (markerId) {
        localStorage.setItem(`marker-links-${markerId}`, JSON.stringify(updated));
    }
  };

  if (!markerId) return null;

  return (
    <div className="max-w-4xl mx-auto">
      {/* 헤더 */}
      <motion.div 
        initial={{ y: -20, opacity: 0 }} animate={{ y: 0, opacity: 1 }}
        className={`rounded-2xl p-8 mb-8 shadow-2xl relative overflow-hidden ${color}`}
      >
        <button 
          onClick={() => router.push('/marker')}
          className="absolute top-6 left-6 p-2 bg-black/20 rounded-full hover:bg-black/30 transition-colors text-white/80"
        >
          <ArrowLeft size={20} />
        </button>
        
        <div className="mt-6 text-center">
          <h1 className="text-3xl font-bold tracking-tight">{title}</h1>
          <p className="opacity-70 text-xs mt-2 uppercase tracking-widest">Wiki & Links Collection</p>
        </div>
      </motion.div>

      {/* 링크 리스트 헤더 */}
      <div className="flex justify-between items-center mb-6 px-1">
        <h2 className="text-lg font-bold text-slate-300">Bookmarks ({links.length})</h2>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-slate-800 text-white rounded-lg text-sm font-bold hover:bg-slate-700 border border-slate-700 transition-colors"
        >
          <Plus size={16}/> Add Link
        </button>
      </div>

      {/* 링크 카드 그리드 */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {links.map((link) => (
          <motion.div 
            initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
            key={link.id} 
            className="bg-slate-900/50 p-4 rounded-xl border border-slate-800 flex items-center justify-between group hover:border-slate-600 hover:bg-slate-900 transition-all"
          >
            <a href={link.url} target="_blank" className="flex items-center gap-4 flex-1 overflow-hidden">
              {/* 아이콘: 무조건 Wiki 아이콘(BookOpen)으로 통일 */}
              <div className="p-2.5 bg-slate-950 rounded-lg border border-slate-800 group-hover:border-slate-500 transition-colors">
                <BookOpen size={20} className="text-emerald-400"/>
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-bold text-sm text-slate-200 group-hover:text-white truncate">{link.title}</h3>
                <p className="text-[10px] text-slate-500 truncate mt-0.5">{link.url}</p>
              </div>
            </a>
            
            <button 
              onClick={() => deleteLink(link.id)}
              className="p-2 text-slate-600 hover:text-rose-500 rounded-lg transition-colors opacity-0 group-hover:opacity-100"
            >
              <Trash2 size={16}/>
            </button>
          </motion.div>
        ))}
        
        {links.length === 0 && (
            <div className="col-span-full py-16 text-center text-slate-600 border border-dashed border-slate-800 rounded-xl">
              <p className="text-sm">No links yet. Add your first guide!</p>
            </div>
        )}
      </div>

      {/* 모달 (카테고리 선택 삭제됨) */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
          <motion.div 
            initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }}
            className="bg-slate-900 p-6 rounded-2xl w-full max-w-sm border border-slate-700 shadow-2xl"
          >
            <h3 className="text-lg font-bold mb-5 text-white">Add New Link</h3>
            
            <div className="space-y-4">
              <div>
                <label className="text-[10px] uppercase text-slate-500 font-bold mb-1 block">Title</label>
                <input 
                  type="text" 
                  autoFocus
                  placeholder="e.g. Boss Patterns"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-3 text-white text-sm focus:outline-none focus:border-emerald-500 transition-colors"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && addLink()}
                />
              </div>
              
              <div>
                <label className="text-[10px] uppercase text-slate-500 font-bold mb-1 block">URL</label>
                <input 
                  type="text" 
                  placeholder="https://..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-3 text-white text-sm focus:outline-none focus:border-emerald-500 transition-colors"
                  value={newUrl}
                  onChange={(e) => setNewUrl(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && addLink()}
                />
              </div>
            </div>

            <div className="flex gap-2 justify-end mt-6">
              <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-slate-500 hover:text-white text-sm">Cancel</button>
              <button onClick={addLink} className="px-5 py-2 bg-emerald-600 text-white rounded-lg font-bold hover:bg-emerald-500 text-sm shadow-lg shadow-emerald-900/20">Save</button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
}

export default function GameDetail() {
  return (
    <div className="min-h-screen bg-slate-950 text-white p-6 font-sans">
      <Suspense fallback={<div className="text-center pt-20">Loading...</div>}>
        <GameDetailContent />
      </Suspense>
    </div>
  );
}