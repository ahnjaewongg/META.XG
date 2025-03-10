import React, { useState, useEffect } from 'react';
import styles from './App.module.css'; 
import { Header } from './components/layout/Header';
import { RiotService } from './services/riotService';
import { Match } from './types/Match';
import { ChampionAnalysis } from './components/champion/ChampionAnalysis';
import { Routes, Route, useNavigate, useParams, BrowserRouter as Router } from 'react-router-dom';
import { SummonerPage } from './components/pages/SummonerPage';
import { RankingPage } from './components/pages/RankingPage';
import { CommunityPage } from './components/pages/CommunityPage';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { createTheme } from '@mui/material/styles';
import { AuthProvider } from './auth/AuthContext';
import AuthRoutes, { ProtectedRoute } from './auth/AuthRoutes';

// 전체 테마 설정
const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#0ac8b9', 
    },
    secondary: {
      main: '#937341',
    },
    background: {
      default: '#0A1428', 
      paper: '#091428', 
    },
    text: {
      primary: '#ffffff',
      secondary: '#cdfafa',
    },
    error: {
      main: '#e84057', 
    },
  },
  typography: {
    fontFamily: '"Spiegel", "Roboto", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 700,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 600,
    },
    h3: {
      fontSize: '1.5rem',
      fontWeight: 600,
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 2,
          textTransform: 'none',
          fontWeight: 600,
        },
        containedPrimary: {
          background: 'linear-gradient(to right, #0ac8b9, #0397ab)',
          '&:hover': {
            background: 'linear-gradient(to right, #0ac8b9, #0ac8b9)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundColor: '#091428',
          borderRadius: 4,
          boxShadow: '0 4px 20px rgba(0, 0, 0, 0.25)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
  },
});

interface PatchNote {
    version: string;
    date: string;
    title: string;
    link: string;
}

interface Notice {
    id: number;
    date: string;
    title: string;
    link: string;
}

function App() {
    const [matches, setMatches] = useState<Match[]>([]);
    const [summonerName, setSummonerName] = useState<string>('');
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [rank, setRank] = useState<any>(null);
    const [currentGame, setCurrentGame] = useState<any>(null);
    const [activeTab, setActiveTab] = useState<'overview' | 'champions' | 'ingame'>('overview');
    const [summonerData, setSummonerData] = useState<any>(null);
    const [currentSummoner, setCurrentSummoner] = useState(null);
    const navigate = useNavigate();

    const riotService = new RiotService();

    const loadSummonerData = async (searchName: string) => {
        setIsLoading(true);
        setError(null);
        
        try {
            // 로컬 스토리지에서 캐시된 데이터 확인
            const cachedData = localStorage.getItem(`summoner_${searchName}`);
            const cachedTimestamp = localStorage.getItem(`summoner_${searchName}_timestamp`);
            const now = Date.now();
            const cacheDuration = 10 * 60 * 1000; // 10분 
            
            // 유효 캐시 확인
            if (cachedData && cachedTimestamp && (now - parseInt(cachedTimestamp)) < cacheDuration) {
                console.log(`Using cached data for ${searchName}`);
                const data = JSON.parse(cachedData);
                
                setCurrentSummoner(data.currentSummoner);
                setRank(data.rank);
                setCurrentGame(data.currentGame);
                setMatches(data.matches || []);
                setSummonerData(data.summonerData);
                
                return;
            }
            
            // 없으면 API 호출
            console.log(`Fetching new data for ${searchName}`);
            const summonerData = await riotService.getSummonerByName(searchName);
            if (!summonerData) {
                throw new Error('소환사를 찾을 수 없습니다.');
            }

            // 모든 API 호출 병렬 실행
            const [rankData, gameData, matchHistory, recommendations] = await Promise.all([
                riotService.getSummonerRank(summonerData.id),
                riotService.getCurrentGame(summonerData.id),
                riotService.getMatchHistory(searchName),
                riotService.getChampionRecommendations(searchName)
            ]);

            console.log('소환사 추천 챔피언 데이터:', recommendations);
            
            // 통계 및 챔피언 데이터 확인
            console.log('소환사 랭크 데이터:', rankData);
            console.log('소환사 게임 데이터:', gameData);
            console.log('소환사 매치 히스토리:', matchHistory);
            
            // 평균 통계 데이터가 없는 경우 기본값 설정
            const averageStats = summonerData.averageStats || {
                kills: 0,
                deaths: 0,
                assists: 0,
                cs: 0,
                visionScore: 0
            };
            
            // 챔피언 통계 데이터가 없는 경우 기본값 설정
            const championStats = summonerData.championStats || [];
            
            console.log('소환사 평균 통계 데이터:', averageStats);
            console.log('소환사 챔피언 통계 데이터:', championStats);

            const enhancedSummonerData = {
                ...summonerData,
                championRecommendations: recommendations,
                averageStats: averageStats,
                championStats: championStats
            };

            setCurrentSummoner(summonerData);
            setRank(rankData);
            setCurrentGame(gameData);
            setMatches(matchHistory || []);
            setSummonerData(enhancedSummonerData);
            
            // 캐시 저장
            const cacheData = {
                currentSummoner: summonerData,
                rank: rankData,
                currentGame: gameData,
                matches: matchHistory || [],
                summonerData: enhancedSummonerData
            };
            localStorage.setItem(`summoner_${searchName}`, JSON.stringify(cacheData));
            localStorage.setItem(`summoner_${searchName}_timestamp`, now.toString());
            
        } catch (error: any) {
            console.error('Error loading summoner data:', error);
            setError(error.message || '소환사를 찾을 수 없습니다. 소환사명을 확인해주세요.');
            setMatches([]);
            setCurrentSummoner(null);
            setSummonerData(null);
            setRank(null);
            setCurrentGame(null);
        } finally {
            setIsLoading(false);
        }
    };

    // 특정 소환사의 캐시를 삭제하는 함수
    const clearSummonerCache = (summonerName: string) => {
        localStorage.removeItem(`summoner_${summonerName}`);
        localStorage.removeItem(`summoner_${summonerName}_timestamp`);
        console.log(`Cleared cache for ${summonerName}`);
    };
    
    // 소환사 정보 갱신 함수
    const refreshSummonerData = async (name: string) => {
        try {
            // 클라이언트 측 캐시 삭제
            clearSummonerCache(name);
            
            // 서버 측 캐시도 새로고침
            await riotService.refreshSummonerData(name);
            
            // 새 데이터 로드
            await loadSummonerData(name);
            
            console.log('데이터가 새로고침되었습니다.');
        } catch (error) {
            console.error('데이터 새로고침 오류:', error);
        }
    };

    // handleSearch 함수 수정
    const handleSearch = async (name: string) => {
        setSummonerName(name);
        await loadSummonerData(name);
        console.log("summonerData: " + summonerData);
        navigate(`/summoner/${encodeURIComponent(name)}`);
    };

    // 소환사 정보 초기화 함수
    const resetSummoner = () => {
        setSummonerName('');
        setMatches([]);
        setCurrentSummoner(null);
        setSummonerData(null);
        setRank(null);
        setCurrentGame(null);
        setError(null);
    };

    const patchNotes: PatchNote[] = [
        {
            version: "2025.S1.3",
            date: "2025.02.04",
            title: "2025.S1.3 패치노트",
            link: "https://www.leagueoflegends.com/ko-kr/news/game-updates/patch-2025-s1-3-notes/"
        },
        {
            version: "2025.S1.2",
            date: "2025.01.22",
            title: "2025.S1.2 패치노트",
            link: "https://www.leagueoflegends.com/ko-kr/news/game-updates/patch-25-s1-2-notes/"
        },
        {
            version: "2025.S1.1",
            date: "2025.01.07",
            title: "2025.S1.1 패치노트",
            link: "https://www.leagueoflegends.com/ko-kr/news/game-updates/patch-25-s1-1-notes/"
        },
        {
            version: "14.24",
            date: "2024.12.10",
            title: "14.24 패치노트",
            link: "https://www.leagueoflegends.com/ko-kr/news/game-updates/patch-14-24-notes/"
        }
    ];

    const notices: Notice[] = [
        {
            id: 5,
            date: "2025.02.17",
            title: "META.XG 서비스 업데이트 안내",
            link: "https://www.notion.so/25-02-17-Update-META-XG-Story-Open-195dd081d5c9818a8e45c11682472925"
        },
        {
            id: 4,
            date: "2025.02.13",
            title: "META.XG 서비스 업데이트 안내",
            link: "https://www.notion.so/25-02-13-Update-META-XG-Story-Open-195dd081d5c9818a8e45c11682472925"
        },
        {
            id: 3,
            date: "2025.02.11",
            title: "META.XG 서비스 업데이트 안내",
            link: "https://www.notion.so/25-02-11-Update-META-XG-Story-Open-195dd081d5c9818a8e45c11682472925"
        },
        {
            id: 2,
            date: "2025.02.10",
            title: "META.XG 서비스 업데이트 안내",
            link: "https://www.notion.so/25-02-10-Update-META-XG-Story-Open-195dd081d5c9818a8e45c11682472925"
        },
        {
            id: 1,
            date: "2025.02.09",
            title: "META.XG 서비스 업데이트 안내",
            link: "https://www.notion.so/25-02-09-Update-META-XG-Story-Open-195dd081d5c9818a8e45c11682472925"
        },
    ];

    return (
        <Router>
            <AuthProvider>
                <ThemeProvider theme={theme}>
                    <CssBaseline />
                    <div className={styles.app}>
                        <Header onSearch={handleSearch} patchNotes={patchNotes} onResetSummoner={resetSummoner} />
                        <Routes>
                            <Route path="/" element={
                                <div className={styles.container}>
                                    <div className={styles.content}>
                                        <div className={styles.mainContent}>
                                            <div className={styles.rightSection}>
                                                <div className={styles.patchNotes}>
                                                    <div className={styles.sectionHeader}>
                                                        <h2>패치 노트</h2>
                                                    </div>
                                                    <div className={styles.notesList}>
                                                        {patchNotes.map((note) => (
                                                            <div key={note.version} onClick={() => window.open(note.link, '_blank')}>
                                                                {note.title}
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                                <div className={styles.notices}>
                                                    <div className={styles.sectionHeader}>
                                                        <h2>공지사항</h2>
                                                    </div>
                                                    <div className={styles.noticeList}>
                                                        {notices.map((notice) => (
                                                            <div key={notice.id} onClick={() => window.open(notice.link, '_blank')}>
                                                                {notice.title}
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            } />
                            <Route path="/summoner/:name" element={<SummonerPage />} />
                            <Route path="/rankings" element={<RankingPage />} />
                            <Route path="/champions" element={<ChampionAnalysis />} />
                            <Route path="/community" element={<CommunityPage />} />
                        </Routes>
                    </div>
                </ThemeProvider>
                <AuthRoutes />
            </AuthProvider>
        </Router>
    );
}

export default App;