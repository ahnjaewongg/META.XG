import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import styles from './SummonerPage.module.css';
import { Box, Tab, Tabs, Typography, Button, CircularProgress } from '@mui/material';
import SyncIcon from '@mui/icons-material/Sync';
import { SummonerProfile } from '../summoner/SummonerProfile';
import { NavigationTabs } from '../layout/NavigationTabs';
import { SummonerContent } from '../summoner/SummonerContent';
import { ChampionRecommendations } from '../summoner/ChampionRecommendations';
import { MentalAnalysis } from '../summoner/MentalAnalysis';
import { RiotService } from '../../services/riotService';
import { Header } from '../layout/Header';
import { v4 as uuidv4 } from 'uuid';
import { MatchHistory } from '../match/MatchHistory';

interface SummonerPageProps {
    currentSummoner?: any;
    rank?: any;
    currentGame?: any;
    matches?: any[];
    summonerData?: any;
    isLoading?: boolean;
    error?: string | null;
    onRefresh?: (name: string) => void;
}

const createMockMatches = (summonerName: string, count: number): any[] => {
    const champions = [
        'Aatrox', 'Ahri', 'Akali', 'Akshan', 'Alistar', 'Amumu', 'Anivia', 'Annie', 'Aphelios', 'Ashe',
        'Aurelion Sol', 'Azir', 'Bard', 'Blitzcrank', 'Brand', 'Braum', 'Caitlyn', 'Camille', 'Cassiopeia', 'Cho\'Gath'
    ];
    
    return Array.from({ length: count }, (_, index) => ({
        metadata: {
            matchId: uuidv4(),
            participants: Array(10).fill(null).map(() => ({
                puuid: uuidv4()
            }))
        },
        info: {
            gameCreation: Date.now() - (86400000 * (index + 1)),
            gameDuration: Math.floor(Math.random() * 2400) + 1200,
            participants: Array(10).fill(null).map((_, i) => ({
                summonerName: i === 0 ? summonerName : `Player${i+1}`,
                championName: champions[Math.floor(Math.random() * champions.length)],
                kills: Math.floor(Math.random() * 15),
                deaths: Math.floor(Math.random() * 10),
                assists: Math.floor(Math.random() * 20),
                totalDamageDealtToChampions: Math.floor(Math.random() * 30000) + 5000,
                goldEarned: Math.floor(Math.random() * 15000) + 8000,
                totalMinionsKilled: Math.floor(Math.random() * 200) + 50,
                win: Math.random() > 0.5,
                teamId: i < 5 ? 100 : 200,
                item0: Math.floor(Math.random() * 3000) + 1000,
                item1: Math.floor(Math.random() * 3000) + 1000,
                item2: Math.floor(Math.random() * 3000) + 1000,
                item3: Math.floor(Math.random() * 3000) + 1000,
                item4: Math.floor(Math.random() * 3000) + 1000,
                item5: Math.floor(Math.random() * 3000) + 1000,
                item6: Math.floor(Math.random() * 3000) + 1000,
                summoner1Id: Math.floor(Math.random() * 10) + 1,
                summoner2Id: Math.floor(Math.random() * 10) + 1
            }))
        }
    }));
};

export const SummonerPage: React.FC<SummonerPageProps> = ({
    currentSummoner: propCurrentSummoner,
    rank: propRank,
    currentGame: propCurrentGame,
    matches: propMatches,
    summonerData: propSummonerData,
    isLoading: propIsLoading,
    error: propError,
    onRefresh: propOnRefresh
}) => {
    const { name } = useParams<{ name: string }>();
    
    const [currentSummoner, setCurrentSummoner] = useState<any>(propCurrentSummoner || null);
    const [rank, setRank] = useState<any>(propRank || null);
    const [currentGame, setCurrentGame] = useState<any>(propCurrentGame || null);
    const [matches, setMatches] = useState<any[]>(propMatches || []);
    const [summonerData, setSummonerData] = useState<any>(propSummonerData || null);
    const [isLoading, setIsLoading] = useState<boolean>(propIsLoading || false);
    const [error, setError] = useState<string | null>(propError || null);
    const [activeTab, setActiveTab] = useState<'overview' | 'champions' | 'ingame'>('overview');
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [isInGame, setIsInGame] = useState(false);
    
    const riotService = new RiotService();
    
    useEffect(() => {
        if (name && !summonerData) {
            loadSummonerData(name);
        }
    }, [name, summonerData]);
    
    useEffect(() => {
        if (currentGame && Object.keys(currentGame).length > 0) {
            setIsInGame(true);
        }
    }, [currentGame]);
    
    const loadSummonerData = async (summonerName: string) => {
        try {
            setIsLoading(true);
            setError(null);
            
            const summonerResponse = await riotService.getSummonerByName(summonerName);
            
            if (!summonerResponse || !summonerResponse.id) {
                throw new Error('소환사를 찾을 수 없습니다.');
            }
            
            const rankResponse = await riotService.getSummonerRank(summonerResponse.id);
            setRank(rankResponse);
            
            const currentGameResponse = await riotService.getCurrentGame(summonerResponse.id);
            setCurrentGame(currentGameResponse);
            
            let matchDetails: any[] = [];
            try {
                const matchIds = await riotService.getMatchList(summonerResponse.puuid);
                if (matchIds && matchIds.length > 0) {
                    matchDetails = await Promise.all(
                        matchIds.slice(0, 10).map((id: string) => riotService.getMatchDetails(id))
                    );
                    setMatches(matchDetails.filter((match: any) => match !== null));
                }
            } catch (error) {
                console.error('Failed to fetch match data:', error);
                matchDetails = [];
            }
            
            setCurrentSummoner(summonerResponse);
            
            const data = {
                summoner: summonerResponse,
                rank: rankResponse,
                currentGame: currentGameResponse,
                matches: matchDetails
            };

            setSummonerData(data);
        } catch (error) {
            console.error('Error loading summoner data:', error);
            setError(error instanceof Error ? error.message : 'An error occurred');
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    };
    
    const handleRefresh = () => {
        if (currentSummoner && currentSummoner.name && !isRefreshing) {
            setIsRefreshing(true);
            if (propOnRefresh) {
                propOnRefresh(currentSummoner.name);
            } else {
                loadSummonerData(currentSummoner.name);
            }
        }
    };

    const handleGameStatusChange = (isInGame: boolean) => {
        setIsInGame(isInGame);
    };

    const handleTabChange = (event: React.SyntheticEvent, newValue: string) => {
        setActiveTab(newValue as 'overview' | 'champions' | 'ingame');
    };

    if (isLoading) {
        return (
            <div className={styles.container}>
                <Header onSearch={(name) => loadSummonerData(name)} />
                <div className={styles.loadingContainer}>
                    <CircularProgress size={60} />
                    <Typography variant="h6" className={styles.loadingText}>
                        소환사 정보를 불러오는 중...
                    </Typography>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.container}>
                <Header onSearch={(name) => loadSummonerData(name)} />
                <div className={styles.errorContainer}>
                    <Typography variant="h5" color="error">
                        오류가 발생했습니다
                    </Typography>
                    <Typography variant="body1" color="error">
                        {error}
                    </Typography>
                </div>
            </div>
        );
    }

    if (!currentSummoner) {
        return (
            <div className={styles.container}>
                <Header onSearch={(name) => loadSummonerData(name)} />
                <div className={styles.emptyContainer}>
                    <Typography variant="h5">
                        소환사를 검색해주세요
                    </Typography>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <Header onSearch={(name) => loadSummonerData(name)} />
            <div className={styles.content}>
                <div className={styles.summonerInfo}>
                    <div className={styles.profileHeader}>
                        <SummonerProfile
                            summonerName={currentSummoner?.name}
                            summonerData={summonerData}
                            onRefresh={handleRefresh}
                        />
                        <Button
                            variant="contained"
                            className={styles.refreshButton}
                            onClick={handleRefresh}
                            disabled={isRefreshing}
                            startIcon={<SyncIcon />}
                        >
                            {isRefreshing ? '새로고침 중...' : '새로고침'}
                        </Button>
                    </div>

                    <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                        <Tabs value={activeTab} onChange={handleTabChange}>
                            <Tab label="개요" value="overview" />
                            <Tab label="챔피언 분석" value="champions" />
                            {isInGame && <Tab label="멘탈 분석" value="ingame" />}
                        </Tabs>
                    </Box>

                    {activeTab === 'overview' && (
                        <div className={styles.matchHistoryContainer}>
                            <Typography variant="h6" className={styles.sectionTitle}>
                                최근 전적
                            </Typography>
                            <MatchHistory
                                matches={matches}
                                summonerName={currentSummoner?.name || ''}
                                puuid={currentSummoner?.puuid || ''}
                            />
                        </div>
                    )}

                    {activeTab === 'champions' && (
                        <ChampionRecommendations 
                            recommendations={summonerData?.championRecommendations}
                            isLoading={isLoading}
                        />
                    )}

                    {activeTab === 'ingame' && currentGame && Object.keys(currentGame).length > 0 && (
                        <MentalAnalysis
                            summonerName={currentSummoner?.name || ''}
                        />
                    )}
                </div>
            </div>
        </div>
    );
}; 


