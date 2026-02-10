"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { api } from "@/lib/api";

// --- 타입 정의 ---
export interface LinkItem {
  id: string;
  title: string;
  url: string;
}

export interface Marker {
  id: string;
  title: string;
  color: string;
  desc?: string;
  links: LinkItem[]; // ★ 링크까지 통째로 들고 있음
}

interface MarkerContextType {
  markers: Marker[];
  isLoading: boolean;
  createMarker: (title: string, color: string) => Promise<void>;
  deleteMarker: (id: string) => Promise<void>;
  addLink: (markerId: string, title: string, url: string) => Promise<void>;
  deleteLink: (markerId: string, linkId: string) => Promise<void>;
}

const MarkerContext = createContext<MarkerContextType | null>(null);

export function MarkerProvider({ children }: { children: ReactNode }) {
  const [markers, setMarkers] = useState<Marker[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // 1. 초기 로딩: 한 번에 싹 다 가져오기
  useEffect(() => {
    const fetchAll = async () => {
      try {
        // 백엔드: GET /api/markers (링크 포함해서 줘야 함!)
        const data = await api.get("/api/markers");
        
        // 데이터 정제 (ID 숫자를 문자로)
        const formatted = data.map((m: any) => ({
          ...m,
          id: m.id.toString(),
          links: m.links ? m.links.map((l: any) => ({...l, id: l.id.toString()})) : []
        }));
        setMarkers(formatted);
      } catch (e) {
        console.error("데이터 로딩 실패", e);
      } finally {
        setIsLoading(false);
      }
    };
    fetchAll();
  }, []);

  // 2. 마커 생성
  const createMarker = async (title: string, color: string) => {
    // 서버 요청
    const newId = await api.post("/api/markers", { title, color });
    
    // 로컬 업데이트 (서버 다시 안 부르고 내가 만든 거 바로 추가)
    const newMarker: Marker = {
      id: newId.toString(),
      title,
      color,
      desc: "Game Guide",
      links: []
    };
    setMarkers(prev => [newMarker, ...prev]);
  };

  // 3. 마커 삭제
  const deleteMarker = async (id: string) => {
    await api.delete(`/api/markers/${id}`);
    setMarkers(prev => prev.filter(m => m.id !== id));
  };

  // 4. 링크 추가 (핵심: 해당 마커를 찾아서 링크 배열에 푸시)
  const addLink = async (markerId: string, title: string, url: string) => {
    // 서버 요청
    const newLinkId = await api.post(`/api/markers/${markerId}/links`, { title, url });
    
    // 로컬 업데이트 (매우 중요: 전체 로딩 안 하고 부분 업데이트)
    setMarkers(prev => prev.map(m => {
      if (m.id === markerId) {
        return {
          ...m,
          links: [...m.links, { id: newLinkId.toString(), title, url }]
        };
      }
      return m;
    }));
  };

  // 5. 링크 삭제
  const deleteLink = async (markerId: string, linkId: string) => {
    await api.delete(`/api/links/${linkId}`); // API 주소 확인 필요
    
    setMarkers(prev => prev.map(m => {
      if (m.id === markerId) {
        return {
          ...m,
          links: m.links.filter(l => l.id !== linkId)
        };
      }
      return m;
    }));
  };

  return (
    <MarkerContext.Provider value={{ markers, isLoading, createMarker, deleteMarker, addLink, deleteLink }}>
      {children}
    </MarkerContext.Provider>
  );
}

// 편하게 쓰기 위한 훅
export function useMarkers() {
  const context = useContext(MarkerContext);
  if (!context) throw new Error("useMarkers must be used within MarkerProvider");
  return context;
}