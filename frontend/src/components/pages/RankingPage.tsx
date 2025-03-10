import React, { useEffect, useState } from 'react';
import { RiotService } from '../../services/riotService';
import styles from './RankingPage.module.css';

interface RankingEntry {
    summonerId: string;
    summonerName: string | null;
    leaguePoints: number;
    wins: number;
    losses: number;
    rank: string;
    tier: string;
    queueType: string;
}

interface PageInfo {
    content: RankingEntry[];
    totalPages: number;
    totalElements: number;
    number: number;
}

export const RankingPage: React.FC = () => {
    const [rankings, setRankings] = useState<RankingEntry[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [selectedTier, setSelectedTier] = useState<string>('ALL');
    const riotService = new RiotService();

    useEffect(() => {
        const loadRankings = async () => {
            setIsLoading(true);
            setError(null);
            try {
                const response: PageInfo = await riotService.getRankings(page, selectedTier !== 'ALL' ? selectedTier : undefined);
                setRankings(response.content);
                setTotalPages(response.totalPages);
            } catch (error) {
                setError('랭킹 정보를 불러오는데 실패했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        loadRankings();
    }, [page, selectedTier]);  

    const handlePageChange = (newPage: number) => {
        setPage(newPage);
    };

    const tiers = ['ALL', 'CHALLENGER', 'GRANDMASTER', 'MASTER', 'DIAMOND', 'PLATINUM', 'GOLD'];

    if (isLoading) return <div className={styles["loading"]}>
        <div className="loading-spinner"></div>
        <p>랭킹 정보를 불러오는 중...</p>
    </div>;
    
    if (error) return <div className={styles["error-message"]}>
        <div className="error-icon">⚠️</div>
        <p>{error}</p>
        <button onClick={() => {
            setPage(0);
            setSelectedTier('ALL');
        }}>다시 시도</button>
    </div>;

    return (
        <div className={styles["ranking-page"]}>
            <h1 className={styles["ranking-title"]}>KR 솔로랭크 TOP 1000</h1>
            
            <div className={styles["tier-selector"]}>
                {tiers.map((tier) => (
                    <button 
                        key={tier}
                        className={selectedTier === tier ? styles["active"] : ''}
                        onClick={() => {
                            setSelectedTier(tier);
                            setPage(0);
                        }}
                    >
                        {tier === 'ALL' ? '전체 티어' : tier}
                    </button>
                ))}
            </div>
            
            <div className={styles["rankings-table"]}>
                <table>
                    <thead>
                        <tr>
                            <th>순위</th>
                            <th>소환사명</th>
                            <th>티어</th>
                            <th>LP</th>
                            <th>승률</th>
                        </tr>
                    </thead>
                    <tbody>
                        {rankings.map((entry, index) => (
                            <tr key={`${entry.summonerId}-${index}`}>
                                <td>{page * 50 + index + 1}</td>
                                <td>{entry.summonerName || '불러오는 중...'}</td>
                                <td>{`${entry.tier} ${entry.rank}`}</td>
                                <td>{entry.leaguePoints}</td>
                                <td>{`${entry.wins}승 ${entry.losses}패 `}
                                    ({(((entry.wins / (entry.wins + entry.losses)) * 100)).toFixed(1)}%)
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            <div className={styles["pagination"]}>
                <button
                    className={styles["pagination-button"]}
                    onClick={() => handlePageChange(0)}
                    disabled={page === 0}
                >
                    처음
                </button>
                <button
                    className={styles["pagination-button"]}
                    onClick={() => handlePageChange(page - 1)}
                    disabled={page === 0}
                >
                    이전
                </button>
                <span className={styles["page-info"]}>
                    {page * 50 + 1}-{Math.min((page + 1) * 50, 1000)} 위
                </span>
                <button
                    className={styles["pagination-button"]}
                    onClick={() => handlePageChange(page + 1)}
                    disabled={page === totalPages - 1}
                >
                    다음
                </button>
                <button
                    className={styles["pagination-button"]}
                    onClick={() => handlePageChange(totalPages - 1)}
                    disabled={page === totalPages - 1}
                >
                    마지막
                </button>
            </div>
        </div>
    );
}; 