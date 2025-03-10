import React, { useState, useEffect } from 'react';
import { ChampionCard } from './ChampionCard';
import styles from './ChampionAnalysis.module.css';
import { ChampionStatistics } from '../../types/ChampionStatistics';
import axios from 'axios';
import { config } from '../../config/config';

export const ChampionAnalysis: React.FC = () => {
    const [champions, setChampions] = useState<ChampionStatistics[]>([]);
    const [selectedPosition, setSelectedPosition] = useState('ALL');
    const [searchTerm, setSearchTerm] = useState('');
    const [sortOption, setSortOption] = useState('tier');
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchChampions();
    }, []);

    const fetchChampions = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('token');
            const headers = {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                ...(token ? { 'Authorization': `Bearer ${token}` } : {})
            };
            
            const response = await axios.get(`${config.API_BASE_URL}/api/champions/statistics`, {
                headers,
                withCredentials: true
            });
            
            setChampions(response.data);
        } catch (error) {
            console.error('Error fetching champions:', error);
            setError('챔피언 데이터를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } finally {
            setIsLoading(false);
        }
    };

    const positions = ['ALL', 'TOP', 'JUNGLE', 'MIDDLE', 'BOTTOM', 'SUPPORT'];

    const tierOrder = {
        'S+': 1, 'S': 2, 'A+': 3, 'A': 4,
        'B+': 5, 'B': 6, 'C+': 7, 'C': 8,
        'D+': 9, 'D': 10, 'E+': 11, 'E': 12,
    };

    const filteredChampions = champions
        .filter(champion => {
            if (!champion?.nameKo) return false;
            
            const matchesSearch = searchTerm 
                ? champion.nameKo.toLowerCase().includes(searchTerm.toLowerCase())
                : true;
                
            const matchesPosition = selectedPosition === 'ALL' || 
                (champion.positions && champion.positions.includes(
                  selectedPosition
                ));
                
            return matchesSearch && matchesPosition;
        })
        .sort((a, b) => {
            if (sortOption === 'tier') {
                return (tierOrder[a.tier as keyof typeof tierOrder] || 7) - (tierOrder[b.tier as keyof typeof tierOrder] || 7);
            }
            if (sortOption === 'winRate') return (b.winRate || 0) - (a.winRate || 0);
            if (sortOption === 'pickRate') return (b.pickRate || 0) - (a.pickRate || 0);
            if (sortOption === 'banRate') return (b.banRate || 0) - (a.banRate || 0);
            return 0;
        });

    if (isLoading) {
        return (
            <div className={styles.container}>
                <div className={styles.loading}>
                    <div className="loading-spinner"></div>
                    <p>챔피언 데이터를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.container}>
                <div className={styles.error}>
                    <h3>오류 발생</h3>
                    <p>{error}</p>
                    <button onClick={fetchChampions}>다시 시도</button>
                </div>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <h1 className={styles.pageTitle}>챔피언 분석</h1>
            <div className={styles.main}>
                <div className={styles.filters}>
                    <div className={styles.filterHeader}>
                        <div className={styles.positionFilters}>
                            {positions.map(position => (
                                <button
                                    key={position}
                                    className={`${styles.positionBtn} ${selectedPosition === position ? styles.active : ''}`}
                                    onClick={() => setSelectedPosition(position)}
                                >
                                    {position === 'ALL' ? 'ALL' : (
                                        <img 
                                            src={`/images/positions/${position.toLowerCase()}.png`}
                                            alt={position}
                                        />
                                    )}
                                </button>
                            ))}
                        </div>
                        <div className={styles.searchSort}>
                            <input
                                type="text"
                                placeholder="챔피언 검색"
                                className={styles.search}
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <select 
                                className={styles.sort}
                                value={sortOption}
                                onChange={(e) => setSortOption(e.target.value)}
                            >
                                <option value="tier">티어순</option>
                                <option value="winRate">승률순</option>
                                <option value="pickRate">픽률순</option>
                                <option value="banRate">밴률순</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div className={styles.grid}>
                    {filteredChampions.length > 0 ? (
                        filteredChampions.map(champion => (
                            <ChampionCard 
                                key={champion.championId} 
                                champion={champion}
                            />
                        ))
                    ) : (
                        <div className={styles.noResults}>
                            <p>검색 결과가 없습니다.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}; 