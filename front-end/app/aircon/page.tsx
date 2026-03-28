"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Thermometer, ChevronUp, ChevronDown, Flame, Snowflake } from "lucide-react";
import { api } from "@/lib/api"

export default function AirconPage() {
  const [temperature, setTemperature] = useState<number | string>("--");
  const [isLoading, setIsLoading] = useState<boolean>(true);


  // 1. 초기 온도 로드
  useEffect(() => {
    fetchTemperature();
  }, []);

  const fetchTemperature = async () => {
    try {
      setIsLoading(true);
      const data = await api.get('/api/aircon');
      if (data && data.temperature !== undefined) {
        setTemperature(data.temperature);
      }
    } catch (error) {
      console.error("온도 조회 실패:", error);
      setTemperature("Err");
    } finally {
      setIsLoading(false);
    }
  };

  const changeTemp = async (action: "up" | "down") => {
    try {
      const data = await api.post(`/api/aircon/${action}`, {});
      if (data && data.temperature !== undefined) {
        setTemperature(data.temperature);
      }
    } catch (error) {
      console.error("온도 조절 실패:", error);
    }
  };

  return (
    <div className="min-h-screen bg-background flex flex-col items-center justify-center p-4">
      <div className="text-center mb-8">
        <h1 className="text-4xl font-bold text-foreground mb-2">동시성 테스트</h1>
        <p className="text-muted-foreground text-lg">에어컨 조작하기</p>
      </div>

      <Card className="w-full max-w-sm shadow-xl border-accent/20">
        <CardHeader className="text-center pb-2">
          <CardTitle className="flex items-center justify-center gap-2 text-2xl">
            <Thermometer className="h-6 w-6 text-accent" />
            Public Aircon
          </CardTitle>
          <CardDescription>공용 에어컨 컨트롤러</CardDescription>
        </CardHeader>
        
        <CardContent className="flex flex-col items-center">
          {/* 온도 디스플레이 */}
          <div className="relative flex items-start justify-center my-8">
            <span className={`text-8xl font-bold text-foreground tracking-tighter transition-opacity ${isLoading ? 'opacity-50' : 'opacity-100'}`}>
              {temperature}
            </span>
            <span className="text-3xl font-medium text-muted-foreground mt-2 ml-1">
              °C
            </span>
          </div>

          {/* 컨트롤 버튼 그룹 */}
          <div className="flex gap-6 mt-4 w-full px-4">
            <Button 
              variant="outline" 
              size="lg"
              className="flex-1 h-20 rounded-2xl bg-blue-50/50 hover:bg-blue-100 dark:bg-blue-950/20 dark:hover:bg-blue-900/40 border-blue-200 dark:border-blue-800 transition-all active:scale-95"
              onClick={() => changeTemp("down")}
            >
              <div className="flex flex-col items-center gap-1">
                <ChevronDown className="h-8 w-8 text-blue-500" />
                <span className="text-xs font-semibold text-blue-600 dark:text-blue-400">DOWN</span>
              </div>
            </Button>
            
            <Button 
              variant="outline" 
              size="lg"
              className="flex-1 h-20 rounded-2xl bg-red-50/50 hover:bg-red-100 dark:bg-red-950/20 dark:hover:bg-red-900/40 border-red-200 dark:border-red-800 transition-all active:scale-95"
              onClick={() => changeTemp("up")}
            >
              <div className="flex flex-col items-center gap-1">
                <ChevronUp className="h-8 w-8 text-red-500" />
                <span className="text-xs font-semibold text-red-600 dark:text-red-400">UP</span>
              </div>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}