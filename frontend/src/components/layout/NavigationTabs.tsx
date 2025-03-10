import React from 'react';
import styles from './NavigationTabs.module.css';

type TabType = 'overview' | 'champions' | 'ingame';

interface NavigationTabsProps {
    activeTab: TabType;
    onTabChange: (tab: TabType) => void;
    hasCurrentGame?: boolean;
}

export const NavigationTabs: React.FC<NavigationTabsProps> = ({ 
    activeTab, 
    onTabChange,
    hasCurrentGame = false
}) => {
    return (
        <div className={styles["navigation-tabs"]}>
            <button 
                className={`${styles["tab"]} ${activeTab === 'overview' ? styles["active"] : ''}`}
                onClick={() => onTabChange('overview')}
            >
                <span className={styles["label"]}>개요</span>
            </button>
            
            <button 
                className={`${styles.tab} ${activeTab === 'champions' ? styles.active : ''}`}
                onClick={() => onTabChange('champions')}
            >
                <span className={styles["label"]}>챔피언</span>
            </button>
            
            <button 
                className={`${styles.tab} ${activeTab === 'ingame' ? styles.active : ''} ${hasCurrentGame ? styles['has-game'] : ''}`}
                onClick={() => onTabChange('ingame')}
            >
                <span className={styles["label"]}>인게임</span>
                {hasCurrentGame && (
                    <span className={styles["game-indicator"]} title="게임 중"></span>
                )}
            </button>
        </div>
    );
}; 