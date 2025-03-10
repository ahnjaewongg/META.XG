import React, { useState } from 'react';
import styles from './ChampionCard.module.css';
import { ChampionStatistics } from '../../types/ChampionStatistics';

interface ChampionCardProps {
    champion: {
        championId: string;
        championName: string;
        nameKo: string;
        positions: string[];
        winRate: number;
        pickRate: number;
        banRate: number;
        tier: string;
    };
    onClick?: (champion: ChampionStatistics) => void;
}

export const ChampionCard: React.FC<ChampionCardProps> = ({ champion, onClick }) => {
    const [imageLoaded, setImageLoaded] = useState(false);
    const [imageError, setImageError] = useState(false);
    const [showTooltip, setShowTooltip] = useState(false);

    const formatRate = (rate: number | null | undefined) => {
        if (rate === null || rate === undefined) return '0.00';
        return Number(rate).toFixed(2);
    };

    const getChampionImageUrl = (championName: string) => {
        const specialCases: { [key: string]: string } = {
            'Fiddlesticks': 'FiddleSticks',
            'RenataGlasc': 'Renata',
            'Rell': 'Rell',
            'Milio': 'Milio',
            'Smolder': 'Smolder',
            'KSante': 'KSante',
            'KaiSa': 'Kaisa',
            'KhaZix': 'Khazix',
            'LeBlanc': 'Leblanc',
            'NunuWillump': 'Nunu',
            'TahmKench': 'TahmKench',
            'Belveth': 'Belveth',
            'Velkoz': 'Velkoz',
            'XinZhao': 'XinZhao'
        };
        const formattedName = championName.replace(/[^a-zA-Z]/g, '');
        const finalName = specialCases[formattedName] || formattedName;
        return `https://ddragon.leagueoflegends.com/cdn/14.3.1/img/champion/${finalName}.png`;
    };

    const handleCardClick = () => {
        if (onClick) {
            onClick(champion as ChampionStatistics);
        }
    };

    return (
        <div 
            className={styles.card} 
            onClick={handleCardClick}
            onMouseEnter={() => setShowTooltip(true)}
            onMouseLeave={() => setShowTooltip(false)}
            title={`${champion.nameKo || champion.championName} - ${champion.tier} 티어`}
        >
            <div className={styles.portrait}>
                {!imageLoaded && !imageError && (
                    <div className={styles.imagePlaceholder}>
                        <div className="loading-spinner"></div>
                    </div>
                )}
                <img 
                    src={getChampionImageUrl(champion.championName)} 
                    alt={champion.nameKo || champion.championName}
                    onLoad={() => setImageLoaded(true)}
                    onError={(e) => {
                        setImageError(true);
                        e.currentTarget.src = '/images/default-champion.png';
                    }}
                    style={{ display: imageLoaded ? 'block' : 'none' }}
                />
                <div className={styles.tierBadge} data-tier={champion.tier || 'C'}>
                    {champion.tier || 'C'}
                </div>
            </div>
            <div className={styles.info}>
                <div className={styles.name}>{champion.nameKo}</div>
                <div className={styles.positions}>
                    {(champion.positions || []).map((position, index) => (
                        <img
                            key={index}
                            src={`/images/positions/${position.toLowerCase()}.png`}
                            alt={position}
                            className={styles.positionIcon}
                        />
                    ))}
                </div>
                <div className={styles.stats}>
                    <div className={styles.statRow}>
                        <span>승률</span>
                        <span className={styles.winRate}>{formatRate(champion.winRate)}%</span>
                    </div>
                    <div className={styles.statRow}>
                        <span>픽률</span>
                        <span className={styles.pickRate}>{formatRate(champion.pickRate)}%</span>
                    </div>
                    <div className={styles.statRow}>
                        <span>밴률</span>
                        <span className={styles.banRate}>{formatRate(champion.banRate)}%</span>
                    </div>
                </div>
            </div>
            {showTooltip && (
                <div className={styles.tooltip}>
                    <div className={styles.tooltipContent}>
                        <h4>{champion.nameKo}</h4>
                        <p>티어: {champion.tier || 'C'}</p>
                        <p>포지션: {champion.positions?.join(', ') || '없음'}</p>
                        <p>승률: {formatRate(champion.winRate)}%</p>
                        <p>픽률: {formatRate(champion.pickRate)}%</p>
                        <p>밴률: {formatRate(champion.banRate)}%</p>
                    </div>
                </div>
            )}
        </div>
    );
}; 