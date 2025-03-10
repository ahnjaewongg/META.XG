import React, { useState, useEffect } from 'react';
import styles from './SummonerContent.module.css';
import { RiotService } from '../../services/riotService';

interface SummonerContentProps {
    rank: any;
    currentGame: any;
    summonerData: any;
    activeTab: string;
    onGameStatusChange?: (inGame: boolean) => void;
}

export const SummonerContent: React.FC<SummonerContentProps> = ({
    rank,
    currentGame: initialCurrentGame,
    summonerData,
    activeTab,
    onGameStatusChange
}) => {
    const [currentGame, setCurrentGame] = useState(initialCurrentGame);
    const [loadingGame, setLoadingGame] = useState(false);
    const riotService = new RiotService();

    // activeTab이 'ingame'으로 변경되거나 summonerData가 변경될 때 현재 게임 정보 가져오기
    useEffect(() => {
        if (activeTab === 'ingame' && summonerData) {
            fetchCurrentGame();
        }
    }, [activeTab, summonerData]);

    // currentGame이 변경될 때마다 부모 컴포넌트에 알림
    useEffect(() => {
        if (onGameStatusChange) {
            const isInGame = Boolean(currentGame && Object.keys(currentGame).length > 0);
            onGameStatusChange(isInGame);
        }
    }, [currentGame, onGameStatusChange]);

    const fetchCurrentGame = async () => {
        if (!summonerData || !summonerData.id) return;

        setLoadingGame(true);
        try {
            const gameData = await riotService.getCurrentGame(summonerData.id);
            setCurrentGame(gameData);
            
            // 게임 상태 변경 알림
            if (onGameStatusChange) {
                const isInGame = Boolean(gameData && Object.keys(gameData || {}).length > 0);
                onGameStatusChange(isInGame);
            }
        } catch (error) {
            console.error('게임 정보를 가져오는데 실패했습니다:', error);
            setCurrentGame(null);
            
            // 게임 상태 변경 알림 (게임 중이 아님)
            if (onGameStatusChange) {
                onGameStatusChange(false);
            }
        } finally {
            setLoadingGame(false);
        }
    };

    if (!summonerData) {
        return <div className={styles["no-data"]}>소환사 정보를 불러올 수 없습니다.</div>;
    }

    // 승률 계산
    const calculateWinRate = (wins: number, losses: number) => {
        const totalGames = wins + losses;
        return totalGames > 0 ? Math.round((wins / totalGames) * 100) : 0;
    };

    // KDA 계산
    const calculateKDA = (kills: number, deaths: number, assists: number) => {
        if (deaths === 0) return 'Perfect';
        return ((kills + assists) / deaths).toFixed(2);
    };

    // 승률 클래스 결정
    const getWinRateClass = (winRate: number) => {
        if (winRate >= 60) return 'high';
        if (winRate >= 50) return 'medium';
        return 'low';
    };

    const renderCurrentGameStatus = () => {
        // 현재 게임 정보가 없거나 빈 객체인 경우
        if (!currentGame || Object.keys(currentGame).length === 0) {
            return (
                <div className={styles["no-game"]}>
                    <div className={styles["no-game-icon"]}>🎮</div>
                    <div className={styles["no-game-text"]}>
                        <h3>게임 중이 아닙니다</h3>
                        <p>소환사가 게임 중이 아닙니다.</p>
                    </div>
                </div>
            );
        }

        return (
            <div className={styles["in-game"]}>
                <div className={styles["status-icon"]}>🎮</div>
                <div className={styles["status-text"]}>게임 중</div>
                <div className={styles["game-info"]}>
                    <div className={styles["game-type"]}>{currentGame.gameType || currentGame.gameMode}</div>
                    <div className={styles["game-time"]}>
                        {currentGame.gameLength ?
                            `${Math.floor(currentGame.gameLength / 60)}분 ${currentGame.gameLength % 60}초` :
                            '로딩 중'}
                    </div>
                </div>
                {activeTab === 'ingame' && (
                    <div className={styles["ingame-details"]}>
                        <h3>참가자 정보</h3>
                        <div className={styles["teams-container"]}>
                            {/* 블루팀 */}
                            <div className={styles["team"]}>
                                <h4 className={styles["team-name"]}>블루팀</h4>
                                {currentGame.participants &&
                                 currentGame.participants
                                    .filter((p: any) => p.teamId === 100)
                                    .map((participant: any, idx: number) => (
                                        <div key={idx} className={styles["participant"]}>
                                            <img
                                                src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${participant.championId}.png`}
                                                alt={participant.championId}
                                                className={styles["champion-icon"]}
                                                onError={(e) => {
                                                    (e.target as HTMLImageElement).src = '/images/champions/default-champion.png';
                                                }}
                                            />
                                            <span className={styles["summoner-name"]}>{participant.summonerName}</span>
                                        </div>
                                    ))
                                }
                            </div>

                            {/* 레드팀 */}
                            <div className={styles["team"]}>
                                <h4 className={styles["team-name"]}>레드팀</h4>
                                {currentGame.participants &&
                                 currentGame.participants
                                    .filter((p: any) => p.teamId === 200)
                                    .map((participant: any, idx: number) => (
                                        <div key={idx} className={styles["participant"]}>
                                            <img
                                                src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${participant.championId}.png`}
                                                alt={participant.championId}
                                                className={styles["champion-icon"]}
                                                onError={(e) => {
                                                    (e.target as HTMLImageElement).src = '/images/champions/default-champion.png';
                                                }}
                                            />
                                            <span className={styles["summoner-name"]}>{participant.summonerName}</span>
                                        </div>
                                    ))
                                }
                            </div>
                        </div>
                    </div>
                )}
            </div>
        );
    };

    // rank.rank 값이 'III'이므로 로마 숫자를 아라비아 숫자로 변환
    const romanToArabic = (roman: string) => {
        const romanNumerals: { [key: string]: number } = {
            'I': 1,
            'II': 2,
            'III': 3,
            'IV': 4,
            'V': 5
        };
        return romanNumerals[roman] || roman;
    };

    // 티어 이미지 가져오기 (로컬 경로 사용)
    const getTierImageUrl = (tier: string) => {
        if (!tier) return '/images/ranked-emblems/emblem-unranked.png';
        
        // 티어 이름을 소문자로 변환
        const formattedTier = tier.toLowerCase();
        
        return `/images/ranked-emblems/emblem-${formattedTier}.png`;
    };

    // 현재 선택된 탭에 따라 다른 내용 보여주기
    const renderContent = () => {
        if (activeTab === 'ingame') {
            return (
                <div className={styles["ingame-tab-content"]}>
                    {loadingGame ? (
                        <div className={styles.loading}>
                            <div className={styles.spinner}></div>
                            <p>인게임 정보를 불러오는 중...</p>
                        </div>
                    ) : (
                        renderCurrentGameStatus()
                    )}
                </div>
            );
        }

        return (
            <>
                <div className={styles["rank-info"]}>
                    <img 
                        src={getTierImageUrl(rank?.tier)} 
                        alt={rank?.tier || 'Unranked'} 
                        className={styles["tier-image"]} 
                        onError={(e) => {
                            (e.target as HTMLImageElement).src = '/images/ranked-emblems/emblem-iron.png';
                        }}
                    />
                    <div className={styles["rank-details"]}>
                        <div className={styles["rank-tier"]}>{rank?.tier || 'UNRANKED'} {rank?.rank ? romanToArabic(rank.rank) : ''}</div>
                        <div className={styles["rank-lp"]}>{rank?.leaguePoints || 0} LP</div>
                        <div className={styles["rank-winrate"]}>
                            {rank ? (
                                <>
                                    <span className={`${styles["wins"]} ${(rank.wins / (rank.wins + rank.losses)) >= 0.5 ? styles["positive"] : styles["negative"]}`}>
                                        {rank.wins}승
                                    </span>
                                    <span className={styles["losses"]}>
                                        {rank.losses}패
                                    </span>
                                    <span className={styles["win-rate"]}>
                                        ({calculateWinRate(rank.wins, rank.losses)}%)
                                    </span>
                                </>
                            ) : '기록 없음'}
                        </div>
                    </div>
                </div>

                {/* 전체 통계 섹션 */}
                <div className={styles["content-section"]}>
                    <h3 className={styles["section-title"]}>전체 통계</h3>
                    <div className={styles["stats-grid"]}>
                        <div className={styles["stat-card"]}>
                            <div className={styles["stat-title"]}>KDA</div>
                            <div className={styles["stat-value"]}>
                                {summonerData.averageStats ? 
                                    calculateKDA(
                                        summonerData.averageStats.kills, 
                                        summonerData.averageStats.deaths, 
                                        summonerData.averageStats.assists
                                    ) : 'N/A'}
                            </div>
                        </div>
                        
                        <div className={styles["stat-card"]}>
                            <div className={styles["stat-title"]}>평균 CS</div>
                            <div className={styles["stat-value"]}>
                                {summonerData.averageStats ? 
                                    Math.round(summonerData.averageStats.cs) : 'N/A'}
                            </div>
                        </div>
                        
                        <div className={styles["stat-card"]}>
                            <div className={styles["stat-title"]}>평균 시야 점수</div>
                            <div className={styles["stat-value"]}>
                                {summonerData.averageStats ? 
                                    Math.round(summonerData.averageStats.visionScore) : 'N/A'}
                            </div>
                        </div>
                    </div>
                </div>

                {/* 주요 챔피언 섹션 */}
                <div className={styles["content-section"]}>
                    <h3 className={styles["section-title"]}>주요 챔피언</h3>
                    {summonerData.championStats && summonerData.championStats.length > 0 ? (
                        <div className={styles["champion-stats"]}>
                            {summonerData.championStats.slice(0, 5).map((champion: any, index: number) => (
                                <div className={styles["champion-card"]} key={index}>
                                    <img 
                                        className={styles["champion-icon"]} 
                                        src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${champion.championId}.png`} 
                                        alt={champion.championId} 
                                        onError={(e) => {
                                            (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/Aatrox.png';
                                        }}
                                    />
                                    <div className={styles["champion-info"]}>
                                        <div className={styles["champion-name"]}>{champion.championId}</div>
                                        <div className={styles["champion-stats-row"]}>
                                            <span>{champion.games}게임</span>
                                            <span className={`${styles["champion-winrate"]} ${styles[getWinRateClass(champion.winRate)]}`}>
                                                {champion.winRate}%
                                            </span>
                                        </div>
                                        <div className={styles["champion-stats-row"]}>
                                            <span>KDA: {calculateKDA(champion.kills, champion.deaths, champion.assists)}</span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className={styles["no-data"]}>챔피언 정보가 없습니다.</div>
                    )}
                </div>
            </>
        );
    };

    return (
        <div className={styles["summoner-content"]}>
            {renderContent()}
        </div>
    );
};
