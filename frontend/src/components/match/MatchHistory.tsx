import React, { useState, useMemo, useEffect } from 'react';
import styles from './MatchHistory.module.css';
import { Match } from '../../types/Match';
import { GameFilter } from './GameFilter';

interface MatchHistoryProps {
    matches: Match[];
    puuid: string;
    summonerName: string;
}

export const MatchHistory: React.FC<MatchHistoryProps> = ({ matches, puuid, summonerName }) => {
    const [expandedMatch, setExpandedMatch] = useState<string | null>(null);
    const [activeFilter, setActiveFilter] = useState('ALL');
    const [championSearch, setChampionSearch] = useState('');

    // 디버깅을 위한 로그 추가
    useEffect(() => {
        if (matches && matches.length > 0) {
            console.log('First match participants:', matches[0].info.participants);
            console.log('Current user PUUID:', puuid);
        }
    }, [matches, puuid]);
    
    const handleQueueFilterChange = (queueType: string) => {
        console.log('Queue filter changed to:', queueType);
        setActiveFilter(queueType);
    };

    const handleChampionSearch = (championName: string) => {
        console.log('Champion search:', championName);
        setChampionSearch(championName.toLowerCase());
    };

    const toggleMatchDetails = (matchId: string) => {
        setExpandedMatch(expandedMatch === matchId ? null : matchId);
    };

    // 검색한 소환사의 PUUID로 해당 참가자 정보 찾기
    const findPlayerParticipant = (match: Match) => {
        if (!match?.info?.participants) {
            console.log('Invalid match data:', match);
            return null;
        }

        // PUUID로 참가자 찾기
        const participant = match.info.participants.find(p => p.puuid === puuid);

        if (!participant) {
            console.log('Participant not found for PUUID:', puuid);
            console.log('Available PUUIDs:', match.info.participants.map(p => p.puuid));
            return null;
        }

        console.log('Found participant:', {
            name: participant.summonerName,
            champion: participant.championName,
            puuid: participant.puuid
        });

        return participant;
    };

    // 필터링된 매치 목록
    const filteredMatches = useMemo(() => {
        if (!matches || matches.length === 0) {
            console.log('No matches to filter');
            return [];
        }

        console.log('Filtering matches with:', { activeFilter, championSearch });
        
        return matches.filter(match => {
            if (!match || !match.info) {
                console.log('Invalid match data:', match);
                return false;
            }

            const participant = findPlayerParticipant(match);
            if (!participant) return false;

            // 큐 타입 필터링
            if (activeFilter !== 'ALL') {
                const queueTypeMatch = match.info.queueId === getQueueId(activeFilter);
                if (!queueTypeMatch) {
                    console.log('Queue type mismatch:', { expected: getQueueId(activeFilter), actual: match.info.queueId });
                    return false;
                }
            }

            // 챔피언 검색 필터링
            if (championSearch && !participant.championName.toLowerCase().includes(championSearch)) {
                return false;
            }

            return true;
        });
    }, [matches, activeFilter, championSearch, puuid]);

    // 필터링 결과 로깅
    useEffect(() => {
        console.log('Filtered matches:', filteredMatches.length);
        if (filteredMatches.length === 0 && matches.length > 0) {
            console.log('All matches were filtered out. First match data:', matches[0]);
        }
    }, [filteredMatches, matches]);

    if (!matches || matches.length === 0) {
        return (
            <div className={styles["match-history"]}>
                <h3 className={styles.title}>매치 히스토리</h3>
                <div className={styles["no-matches"]}>
                    <p>매치 기록이 없습니다.</p>
                </div>
            </div>
        );
    }

    // 게임 타입 포맷팅
    const formatGameType = (queueId: number) => {
        switch (queueId) {
            case 420:
                return '솔로랭크';
            case 430:
                return '일반게임';
            case 440:
                return '자유랭크';
            case 450:
                return '칼바람';
            case 900:
                return '우르프';
            case 1900:
                return '우르프';
            default:
                return '커스텀';
        }
    };

    // 시간 포맷팅 (몇 시간/일 전)
    const getTimeAgo = (timestamp: number) => {
        const now = new Date();
        const gameTime = new Date(timestamp);
        const diffMs = now.getTime() - gameTime.getTime();
        const diffMins = Math.floor(diffMs / (1000 * 60));
        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
        const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

        if (diffMins < 60) {
            return `${diffMins}분 전`;
        } else if (diffHours < 24) {
            return `${diffHours}시간 전`;
        } else {
            return `${diffDays}일 전`;
        }
    };

    // 게임 시간 포맷팅 (분:초)
    const formatGameDuration = (seconds: number) => {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}분 ${remainingSeconds}초`;
    };

    // KDA 비율 계산 및 포맷팅
    const calculateKdaRatio = (kills: number, deaths: number, assists: number) => {
        if (deaths === 0) {
            return 'Perfect';
        }
        return ((kills + assists) / deaths).toFixed(2);
    };

    // KDA 등급 계산
    const getKdaClass = (kills: number, deaths: number, assists: number) => {
        const ratio = deaths === 0 ? 10 : (kills + assists) / deaths;
        if (deaths === 0) return styles.perfect;
        if (ratio >= 4) return styles.high;
        return styles.low;
    };

    // 스펠과 룬 이미지 경로 처리 함수 추가
    const getSummonerSpellImageUrl = (spellId: number) => {
        // 스펠 ID에 따른 이미지 이름 매핑
        const spellMap: Record<number, string> = {
            1: 'SummonerBoost',       // 정화
            3: 'SummonerExhaust',     // 탈진
            4: 'SummonerFlash',       // 점멸
            6: 'SummonerHaste',       // 유체화
            7: 'SummonerHeal',        // 치유
            11: 'SummonerSmite',      // 강타
            12: 'SummonerTeleport',   // 순간이동
            13: 'SummonerMana',       // 총명
            14: 'SummonerDot',        // 점화
            21: 'SummonerBarrier',    // 방어막
            30: 'SummonerPoroRecall', // 포로 소환
            31: 'SummonerPoroThrow',  // 포로 던지기
            32: 'SummonerSnowball',   // 표식
            39: 'SummonerSnowURFSnowball_Mark', // URF 표식
        };

        const spellName = spellMap[spellId] || 'SummonerFlash'; // 기본값
        return `https://ddragon.leagueoflegends.com/cdn/14.8.1/img/spell/${spellName}.png`;
    };

    const getRuneImageUrl = (runeId: number) => {
        // 주요 룬 페이지 ID
        const runePageMap: Record<number, string> = {
            8000: 'perk-images/Styles/7201_Precision.png',           // 정밀
            8100: 'perk-images/Styles/7200_Domination.png',          // 지배
            8200: 'perk-images/Styles/7202_Sorcery.png',             // 마법
            8300: 'perk-images/Styles/7203_Whimsy.png',              // 영감
            8400: 'perk-images/Styles/7204_Resolve.png',             // 결의
        };

        // 주요 룬 ID (데이터에 따라 확장 필요)
        const runeMap: Record<number, string> = {
            // 정밀 (Precision)
            8005: 'perk-images/Styles/Precision/PressTheAttack/PressTheAttack.png',         // 집중 공격
            8008: 'perk-images/Styles/Precision/LethalTempo/LethalTempoTemp.png',           // 치명적 속도
            8010: 'perk-images/Styles/Precision/Conqueror/Conqueror.png',                   // 정복자
            8021: 'perk-images/Styles/Precision/FleetFootwork/FleetFootwork.png',           // 기민한 발놀림
            
            // 지배 (Domination)
            8112: 'perk-images/Styles/Domination/Electrocute/Electrocute.png',              // 감전
            8124: 'perk-images/Styles/Domination/Predator/Predator.png',                    // 포식자
            8128: 'perk-images/Styles/Domination/DarkHarvest/DarkHarvest.png',              // 어둠의 수확
            9923: 'perk-images/Styles/Domination/HailOfBlades/HailOfBlades.png',            // 칼날비
            
            // 마법 (Sorcery)
            8214: 'perk-images/Styles/Sorcery/SummonAery/SummonAery.png',                  // 콩콩이 소환
            8229: 'perk-images/Styles/Sorcery/ArcaneComet/ArcaneComet.png',                // 신비로운 유성
            8230: 'perk-images/Styles/Sorcery/PhaseRush/PhaseRush.png',                    // 난입
            
            // 결의 (Resolve)
            8437: 'perk-images/Styles/Resolve/GraspOfTheUndying/GraspOfTheUndying.png',     // 착취의 손아귀
            8439: 'perk-images/Styles/Resolve/VeteranAftershock/VeteranAftershock.png',     // 여진
            8465: 'perk-images/Styles/Resolve/Guardian/Guardian.png',                       // 수호자
            
            // 영감 (Inspiration)
            8351: 'perk-images/Styles/Inspiration/GlacialAugment/GlacialAugment.png',       // 빙결 강화
            8360: 'perk-images/Styles/Inspiration/UnsealedSpellbook/UnsealedSpellbook.png', // 봉인 풀린 주문서
            8369: 'perk-images/Styles/Inspiration/FirstStrike/FirstStrike.png',             // 선제공격
        };

        // 룬 ID에 따른 이미지 경로 반환
        if (runeId in runeMap) {
            return `https://ddragon.leagueoflegends.com/cdn/${runeMap[runeId]}`;
        } else if (runeId in runePageMap) {
            return `https://ddragon.leagueoflegends.com/cdn/${runePageMap[runeId]}`;
        }
        
        // 기본값 반환
        return `https://ddragon.leagueoflegends.com/cdn/img/perk-images/Styles/7200_Domination.png`;
    };

    return (
        <div className={styles["match-history"]}>
            <h3 className={styles.title}>매치 히스토리</h3>
            <GameFilter 
                onQueueFilterChange={handleQueueFilterChange}
                onChampionSearch={handleChampionSearch}
                activeFilter={activeFilter}
            />
            <div className={styles["match-list"]}>
                {filteredMatches.length === 0 ? (
                    <div className={styles["no-matches"]}>필터링된 매치가 없습니다.</div>
                ) : (
                    filteredMatches.map((match) => {
                        if (!match || !match.info) {
                            console.log('Invalid match data:', match);
                            return null;
                        }
                        
                        const participant = findPlayerParticipant(match);
                        if (!participant) {
                            console.log('Participant not found for match:', match.metadata.matchId);
                            return null;
                        }

                        // DTO 구조에 맞게 데이터 추출
                        const win = participant.win;
                        const gameType = formatGameType(match.info.queueId);
                        const timeAgo = getTimeAgo(match.info.gameCreation);
                        const duration = formatGameDuration(match.info.gameDuration);
                        const kdaRatio = calculateKdaRatio(
                            participant.kills,
                            participant.deaths,
                            participant.assists
                        );
                        const kdaClass = getKdaClass(
                            participant.kills,
                            participant.deaths,
                            participant.assists
                        );
                        
                        // 팀 구분
                        const team1 = match.info.participants.filter((p: any) => p.teamId === 100).slice(0, 5);
                        const team2 = match.info.participants.filter((p: any) => p.teamId === 200).slice(0, 5);

                        // 타입 오류를 피하기 위해 any 타입 사용
                        const anyParticipant = participant as any;

                        // 스펠 ID 추출
                        const spell1Id = anyParticipant.summoner1Id || 0;
                        const spell2Id = anyParticipant.summoner2Id || 0;

                        // 룬 ID 추출
                        let primaryRuneId = 0;
                        let secondaryStyleId = 0;

                        // 룬 정보 추출 시도
                        if (anyParticipant.perks?.styles?.[0]?.selections?.[0]?.perk) {
                            primaryRuneId = anyParticipant.perks.styles[0].selections[0].perk;
                        }

                        if (anyParticipant.perks?.styles?.[1]?.style) {
                            secondaryStyleId = anyParticipant.perks.styles[1].style;
                        }

                        // 스펠과 룬 정보 로깅
                        console.log('Participant data:', {
                            summonerName: participant.summonerName,
                            spell1: spell1Id,
                            spell2: spell2Id, 
                            primaryRune: primaryRuneId,
                            secondaryRune: secondaryStyleId
                        });

                        return (
                            <div 
                                key={match.metadata.matchId} 
                                className={`${styles["match-card"]} ${win ? styles.win : styles.lose}`}
                                onClick={() => toggleMatchDetails(match.metadata.matchId)}
                            >
                                <div className={styles["match-info"]}>
                                    <div className={styles["game-type"]}>{gameType}</div>
                                    <div className={styles["game-time"]}>{timeAgo}</div>
                                    <div className={`${styles["result-text"]} ${win ? styles.win : styles.lose}`}>
                                        {win ? '승리' : '패배'}
                                    </div>
                                    <div className={styles["game-length"]}>{duration}</div>
                                </div>
                                
                                <div className={styles["champion-summary"]}>
                                    <div className={styles["champion-icon-wrapper"]}>
                                        <div className={styles["champion-icon"]}>
                                            <img 
                                                className={styles["champion-icon"]}
                                                src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${participant.championName}.png`}
                                                alt={participant.championName || '알 수 없음'}
                                                onError={(e) => {
                                                    console.log('Champion image load error:', participant.championName);
                                                    (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/Aatrox.png';
                                                }}
                                            />
                                        </div>
                                        <div className={styles["champion-level"]}>
                                            {anyParticipant.champLevel || '?'}
                                        </div>
                                    </div>
                                    
                                    <div className={styles["spell-rune-wrapper"]}>
                                        <div className={styles.spells}>
                                            <div className={styles["spell-icon"]}>
                                                <img 
                                                    src={getSummonerSpellImageUrl(spell1Id)} 
                                                    alt="스펠1" 
                                                    onError={(e) => {
                                                        console.log('Spell image load error:', spell1Id);
                                                        (e.target as HTMLImageElement).src = getSummonerSpellImageUrl(4); // 기본값: 점멸
                                                    }}
                                                />
                                            </div>
                                            <div className={styles["spell-icon"]}>
                                                <img 
                                                    src={getSummonerSpellImageUrl(spell2Id)} 
                                                    alt="스펠2"
                                                    onError={(e) => {
                                                        console.log('Spell image load error:', spell2Id);
                                                        (e.target as HTMLImageElement).src = getSummonerSpellImageUrl(14); // 기본값: 점화
                                                    }}
                                                />
                                            </div>
                                        </div>
                                        <div className={styles.runes}>
                                            <div className={styles["rune-icon"]}>
                                                <img 
                                                    src={getRuneImageUrl(primaryRuneId)} 
                                                    alt="룬1"
                                                    onError={(e) => {
                                                        console.log('Rune image load error:', primaryRuneId);
                                                        (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/img/perk-images/Styles/7200_Domination.png';
                                                    }}
                                                />
                                            </div>
                                            <div className={styles["rune-icon"]}>
                                                <img 
                                                    src={getRuneImageUrl(secondaryStyleId)} 
                                                    alt="룬2"
                                                    onError={(e) => {
                                                        console.log('Rune style image load error:', secondaryStyleId);
                                                        (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/img/perk-images/Styles/7200_Domination.png';
                                                    }}
                                                />
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div className={styles.stats}>
                                        <div className={styles.kda}>
                                            <span>{participant.kills}</span> / <span style={{ color: '#bc3a3a' }}>{participant.deaths}</span> / <span>{participant.assists}</span>
                                        </div>
                                        <div className={`${styles["kda-ratio"]} ${kdaClass}`}>
                                            {kdaRatio === 'Perfect' ? 'Perfect KDA' : `${kdaRatio}:1 평점`}
                                        </div>
                                        <div className={styles["cs-vision"]}>
                                            <span>CS {participant.totalMinionsKilled + (anyParticipant.neutralMinionsKilled || 0)}</span>
                                            <span>시야 {anyParticipant.visionScore || 0}</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div className={styles.items}>
                                    {[0, 1, 2, 3, 4, 5, 6].map((slotNum) => {
                                        const itemKey = `item${slotNum}` as keyof typeof participant;
                                        const itemId = participant[itemKey] as number;
                                        return (
                                            <div key={slotNum} className={`${styles["item-slot"]} ${!itemId ? styles.empty : ''}`}>
                                                {itemId ? (
                                                    <img 
                                                        className={styles["item-icon"]}
                                                        src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/item/${itemId}.png`}
                                                        alt={`아이템 ${slotNum + 1}`}
                                                        onError={(e) => {
                                                            console.log('Item image load error:', itemId);
                                                            (e.target as HTMLImageElement).style.display = 'none';
                                                        }}
                                                    />
                                                ) : null}
                                            </div>
                                        );
                                    })}
                                </div>
                                
                                <div className={styles.participants}>
                                    <div className={styles.team}>
                                        <div className={styles["team-header"]}>블루팀</div>
                                        <div className={styles["team-players"]}>
                                            {team1.map((player: any, idx: number) => (
                                                <div key={idx} className={styles.player}>
                                                    <div className={styles["player-icon"]}>
                                                        <img 
                                                            src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${player.championName}.png`} 
                                                            alt={player.championName} 
                                                            title={player.summonerName}
                                                            onError={(e) => {
                                                                (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/Aatrox.png';
                                                            }}
                                                        />
                                                    </div>
                                                    <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{player.summonerName}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                    <div className={styles.team}>
                                        <div className={styles["team-header"]}>레드팀</div>
                                        <div className={styles["team-players"]}>
                                            {team2.map((player: any, idx: number) => (
                                                <div key={idx} className={styles.player}>
                                                    <div className={styles["player-icon"]}>
                                                        <img 
                                                            src={`https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/${player.championName}.png`} 
                                                            alt={player.championName} 
                                                            title={player.summonerName}
                                                            onError={(e) => {
                                                                (e.target as HTMLImageElement).src = 'https://ddragon.leagueoflegends.com/cdn/14.8.1/img/champion/Aatrox.png';
                                                            }}
                                                        />
                                                    </div>
                                                    <span style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{player.summonerName}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                                
                                <button 
                                    className={styles["details-button"]} 
                                    onClick={(e) => {
                                        e.stopPropagation(); 
                                        toggleMatchDetails(match.metadata.matchId);
                                    }}
                                >
                                    {expandedMatch === match.metadata.matchId ? '접기' : '상세'}
                                </button>
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
};

// 큐 타입 ID 매핑
function getQueueId(queueType: string): number {
    const queueIds: { [key: string]: number } = {
        'RANKED_SOLO_5x5': 420,
        'RANKED_FLEX_SR': 440,
        'NORMAL_5V5_BLIND': 430,
        'ARAM': 450
    };
    return queueIds[queueType] || 0;
}