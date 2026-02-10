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
            <a href={link.url} target="_blank" className="flex