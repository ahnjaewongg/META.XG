import React, { useEffect } from 'react';
import styles from './ChampionRecommendations.module.css';

interface ChampionRecommendation {
  championId: string;
  championName: string;
  position: string;
  winRate: number;
  pickRate: number;
  banRate: number;
  kda: number;
}

interface ChampionRecommendationsProps {
  recommendations: ChampionRecommendation[] | null;
  isLoading: boolean;
}

export const ChampionRecommendations: React.FC<ChampionRecommendationsProps> = ({
  recommendations = [],
  isLoading = false
}) => {
  // 컴포넌트가 마운트될 때만 실행되는 효과
  useEffect(() => {
    console.log("ChampionRecommendations mounted with data:", recommendations);
  }, []);
  
  // 추천 데이터가 변경될 때마다 실행되는 효과
  useEffect(() => {
    console.log("ChampionRecommendations data updated:", recommendations);
  }, [recommendations]);

  if (isLoading) {
    return (
      <div className={styles["recommendations-container"]}>
        <div className={styles["recommendations-header"]}>
          <h3 className={styles["recommendations-title"]}>추천 챔피언</h3>
        </div>
        <div className={styles["loading"]}>
          <div className={styles["loading-spinner"]}></div>
          <p>추천 챔피언을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (!recommendations || recommendations.length === 0) {
    return (
      <div className={styles["recommendations-container"]}>
        <div className={styles["recommendations-header"]}>
          <h3 className={styles["recommendations-title"]}>추천 챔피언</h3>
        </div>
        <div className={styles["no-recommendations"]}>
          <p>추천 챔피언이 없습니다.</p>
          <p>더 많은 게임을 플레이하여 데이터를 쌓아보세요.</p>
        </div>
      </div>
    );
  }

  // KDA 포맷팅
  const formatKDA = (kda: number) => {
    return kda.toFixed(2);
  };

  // 승률 클래스 결정
  const getWinRateClass = (winRate: number) => {
    if (winRate >= 60) return 'high';
    if (winRate >= 50) return 'medium';
    return 'low';
  };

  // 포지션 한글화
  const translatePosition = (position: string) => {
    const positionMap: Record<string, string> = {
      'TOP': '탑',
      'JUNGLE': '정글',
      'MIDDLE': '미드',
      'BOTTOM': '원딜',
      'UTILITY': '서포터'
    };
    return positionMap[position] || position;
  };

  return (
    <div className={styles["recommendations-container"]}>
      <div className={styles["recommendations-header"]}>
        <h3 className={styles["recommendations-title"]}>추천 챔피언</h3>
        <div className={styles["recommendations-info"]}>
          <span>나와 비슷한 플레이어 기반</span>
          <div className={styles["tooltip"]}>
            <span className={styles["info-icon"]}>ⓘ</span>
            <span className={styles["tooltip-text"]}>
              이 추천은 비슷한 실력과 플레이 스타일을 가진 플레이어들의 데이터를 기반으로 합니다.
              당신의 주 포지션과 가장 많이 플레이한 챔피언을 분석하여 좋은 성과를 낼 수 있는 챔피언을 추천합니다.
              <br /><br />
              <strong>추천 기준:</strong>
              <ul style={{ textAlign: 'left', padding: '0px 10px' }}>
                <li>플레이어의 주 포지션</li>
                <li>승률 및 KDA</li>
                <li>현재 메타 강세 챔피언</li>
                <li>플레이어 스타일 유사도</li>
              </ul>
            </span>
          </div>
        </div>
      </div>
      <div className={styles["recommendations-list"]}>
        {recommendations.slice(0, 4).map((recommendation, index) => (
          <div key={index} className={styles["recommendation-card"]}>
            <img 
              className={styles["champion-image"]} 
              src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${recommendation.championName.replace(/\s/g, '')}.png`} 
              alt={recommendation.championName}
              onError={(e) => {
                (e.target as HTMLImageElement).src = '/images/champion-placeholder.png';
              }}
            />
            <div className={styles["champion-name"]}>{recommendation.championName}</div>
            <div className={styles["champion-position"]}>{translatePosition(recommendation.position)}</div>
            <div className={styles["champion-stats"]}>
              <span className={styles["winrate"]}>{recommendation.winRate}% 승률</span>
              <span className={styles["kda"]}>KDA {formatKDA(recommendation.kda)}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}; 