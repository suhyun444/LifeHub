"use client";

import { useState, useEffect, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { ArrowLeft, Plus, Trash2, Youtube, BookOpen, Globe, Map, MessageCircle } from "lucide-react";
import { motion } from "framer-motion";

interface LinkItem {
  id: string;
  title: string;
  url: string;
  category: "Wiki" | "Youtube" | "Community" | "Map" | "Etc";
}

function GameDetailContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  // URL 쿼리스트링에서 정보 가져오기
  const markerId = searchParams.get("id");
  const title = searchParams.get("title") || "Game Guide";
  const color = searchParams.get("color") || "bg-slate-800";

  const [links, setLinks] = useState<LinkItem[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  const [newTitle, setNewTitle] = useState("");
  const [newUrl, setNewUrl] = useState("");
  const [newCategory, setNewCategory] = useState<LinkItem['category']>("Wiki");

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
      category: newCategory,
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

  const getIcon = (cat: string) => {
    switch(cat) {
      case "Youtube": return <Youtube size={18} className="text-red-500"/>;
      case "Wiki": return <BookOpen size={18} className="text-emerald-500"/>;
      case "Map": return <Map size={18} className="text-amber-500"/>;
      case "Community": return <MessageCircle size={18} className="text-blue-500"/>;
      default: return <Globe size={18} className="text-slate-400"/>;
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
          <p className="opacity-70 text-xs mt-2 uppercase tracking-widest">Game Links Collection</p>
        </div>
      </motion.div>

      {/* 링크 리스트 */}
      <div className="flex justify-between items-center mb-6 px-1">
        <h2 className="text-lg font-bold text-slate-300">Links ({links.length})</h2>
        <button 
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-slate-800 text-white rounded-lg text-sm font-bold hover:bg-slate-700 border border-slate-700 transition-colors"
        >
          <Plus size={16}/> Add Link
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {links.map((link) => (
          <motion.div 
            initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }}
            key={link.id} 
            className="bg-slate-900/50 p-4 rounded-xl border border-slate-800 flex items-center justify-between group hover:border-slate-600 hover:bg-slate-900 transition-all"
          >
            <a href={link.url} target="_blank" className="flex items-center gap-4 flex-1 overflow-hidden">
              <div className="p-2.5 bg-slate-950 rounded-lg border border-slate-800 group-hover:border-slate-700">
                {getIcon(link.category)}
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

      {/* 모달 */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
          <motion.div 
            initial={{ y: 20, opacity: 0 }} animate={{ y: 0, opacity: 1 }}
            className="bg-slate-900 p-6 rounded-2xl w-full max-w-sm border border-slate-700 shadow-2xl"
          >
            <h3 className="text-lg font-bold mb-5 text-white">Add New Link</h3>
            
            <div className="space-y-4">
              <div>
                <label className="text-[10px] uppercase text-slate-500 font-bold mb-1 block">Category</label>
                <div className="grid grid-cols-3 gap-2">
                  {["Wiki", "Youtube", "Map", "Community", "Etc"].map((cat) => (
                    <button
                      key={cat}
                      onClick={() => setNewCategory(cat as any)}
                      className={`text-xs py-2 rounded-lg border ${newCategory === cat ? 'bg-white text-black border-white' : 'bg-slate-950 border-slate-800 text-slate-400 hover:border-slate-600'}`}
                    >
                      {cat}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-[10px] uppercase text-slate-500 font-bold mb-1 block">Title</label>
                <input 
                  type="text" 
                  placeholder="e.g. Boss Patterns"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-white text-sm focus:outline-none focus:border-slate-500"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                />
              </div>
              
              <div>
                <label className="text-[10px] uppercase text-slate-500 font-bold mb-1 block">URL</label>
                <input 
                  type="text" 
                  placeholder="https://..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg p-2.5 text-white text-sm focus:outline-none focus:border-slate-500"
                  value={newUrl}
                  onChange={(e) => setNewUrl(e.target.value)}
                />
              </div>
            </div>

            <div className="flex gap-2 justify-end mt-6">
              <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-slate-500 hover:text-white text-sm">Cancel</button>
              <button onClick={addLink} className="px-5 py-2 bg-white text-black rounded-lg font-bold hover:bg-slate-200 text-sm">Save Link</button>
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
      <Suspense fallback={<div className="text-center pt-20">Loading Game Data...</div>}>
        <GameDetailContent />
      </Suspense>
    </div>
  );
}