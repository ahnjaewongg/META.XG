import React, { useState } from 'react';
import styles from './SummonerProfile.module.css';

interface SummonerProfileProps {
    summonerName?: string;
    summonerData: any;
    onRefresh?: () => void;
}

export const SummonerProfile: React.FC<SummonerProfileProps> = ({
    summonerName,
    summonerData,
    onRefresh
}) => {
    const [isFavorite, setIsFavorite] = useState<boolean>(false);

    if (!summonerData) {
        return <div className={styles["summoner-profile"] + " " + styles["loading"]}>로딩 중...</div>;
    }

    // 소환사 이름에서 태그 분리
    const formatSummonerName = () => {
        if (!summonerName) return summonerData.name;
        const parts = summonerName.split('#');
        if (parts.length > 1) {
            return (
                <>
                    {parts[0]}
                    <span className={styles["summoner-name-tag"]}>#{parts[1]}</span>
                </>
            );
        }
        return summonerName;
    };

    // 마지막 업데이트 시간
    const lastUpdated = new Date().toLocaleString('ko-KR', {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });

    // 즐겨찾기 토글 함수
    const toggleFavorite = () => {
        setIsFavorite(!isFavorite);
        // 여기에 로컬 스토리지 또는 서버에 즐겨찾기 정보 저장 로직 추가 가능
        // localStorage.setItem('favorite-' + summonerData.name, !isFavorite ? 'true' : 'false');
    };

    return (
        <div className={styles["summoner-profile"]}>
            <div className={styles["profile-left"]}>
                <div className={styles["profile-icon-container"]}>
                    <img 
                        src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/profileicon/${summonerData.profileIconId}.png`} 
                        alt="프로필 아이콘" 
                        className={styles["profile-icon"]}
                        onError={(e) => {
                            (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/14.8.1/img/profileicon/29.png';
                        }}
                    />
                    <div className={styles["summoner-level"]}>{summonerData.summonerLevel}</div>
                </div>
            </div>
            <div className={styles["profile-info"]}>
                <div className={styles["summoner-name"]}>{summonerName || summonerData.name}</div>
                <div className={styles["last-update"]}>
                    최근 업데이트: {new Date().toLocaleString()}
                    {onRefresh && (
                        <button className={styles["refresh-button"]} onClick={onRefresh}>
                            <span className={styles["refresh-icon"]}>↻</span>
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
};