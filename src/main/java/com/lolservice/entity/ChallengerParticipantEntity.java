package com.lolservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenger_participants")
@Data
@NoArgsConstructor
public class ChallengerParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchId;

    @Column(name = "game_creation")
    private LocalDateTime gameCreation;

    @Column(name = "game_duration")
    private Long gameDuration;

    private String gameVersion;
    private Integer queueId;
    private String queueType;

    private String puuid;
    private String summonerName;
    private String championId;
    private String championName;
    private String position;
    private boolean winner;

    // 기본 스탯
    private int kills;
    private int deaths;
    private int assists;
    private int visionScore;
    private int goldEarned;
    private int totalMinionsKilled = 0;
    private int neutralMinionsKilled = 0;

    // 멀티킬 정보 (기본값 0으로 설정)
    private int doubleKills = 0;
    private int tripleKills = 0;
    private int quadraKills = 0;
    private int pentaKills = 0;
    private boolean firstBloodKill = false;
    private int turretKills = 0;

    // 아이템 정보
    private int item0;
    private int item1;
    private int item2;
    private int item3;
    private int item4;
    private int item5;
    private int item6; // 장신구

    // 룬 정보
    private int primaryRune;
    private int secondaryRune;

    // 스펠 정보
    private int spell1;
    private int spell2;

    @Column(name = "total_damage_dealt_to_champions")
    private Integer totalDamageDealtToChampions;

    // queueId를 queueType으로 변환하는 메서드
    public void setQueueTypeFromId(int queueId) {
        switch (queueId) {
            case 420:
                this.queueType = "SOLO_RANK";
                break;
            case 440:
                this.queueType = "FLEX_RANK";
                break;
            case 450:
                this.queueType = "ARAM";
                break;
            case 400:
                this.queueType = "NORMAL_DRAFT";
                break;
            case 430:
                this.queueType = "NORMAL_BLIND";
                break;
            case 900:
                this.queueType = "URF";
                break;
            case 1700:
                this.queueType = "ARENA";
                break;
            case 1300:
                this.queueType = "NEXUS_BLITZ";
                break;
            default:
                this.queueType = "OTHER_" + queueId; // 알 수 없는 큐 ID는 번호와 함께 저장
        }
    }

    public void setTotalDamageDealtToChampions(Integer totalDamageDealtToChampions) {
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
    }

    public Integer getTotalDamageDealtToChampions() {
        return totalDamageDealtToChampions;
    }

    // position 설정 시 UTILITY를 SUPPORT로 변환하는 setter 메서드
    public void setPosition(String position) {
        if (position != null && position.equals("UTILITY")) {
            this.position = "SUPPORT";
        } else {
            this.position = position;
        }
    }
}