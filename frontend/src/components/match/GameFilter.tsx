import React from 'react';
import styles from './GameFilter.module.css';

interface GameFilterProps {
    onQueueFilterChange: (queueType: string) => void;
    onChampionSearch: (championName: string) => void;
    activeFilter: string;
}

export const GameFilter: React.FC<GameFilterProps> = ({ 
    onQueueFilterChange, 
    onChampionSearch, 
    activeFilter 
}) => {
    const queueTypes = [
        { id: 'ALL', label: '전체' },
        { id: 'RANKED_SOLO_5x5', label: '솔로랭크' },
        { id: 'RANKED_FLEX_SR', label: '자유랭크' },
        { id: 'NORMAL_5V5_BLIND', label: '빠른대전' },
        { id: 'ARAM', label: '칼바람나락' }
    ];

    return (
        <div className={styles["game-filter"]}>
            <div className={styles["queue-filters"]}>
                {queueTypes.map(queue => (
                    <button
                        key={queue.id}
                        className={`${styles["filter-btn"]} ${activeFilter === queue.id ? styles["active"] : ''}`}
                        onClick={() => onQueueFilterChange(queue.id)}
                    >
                        {queue.label}
                    </button>
                ))}
            </div>
            <div className={styles["champion-search"]}>
                <input
                    type="text"
                    placeholder="챔피언 검색"
                    className={styles["champion-input"]}
                    onChange={(e) => onChampionSearch(e.target.value)}
                />
            </div>
        </div>
    );
}; 