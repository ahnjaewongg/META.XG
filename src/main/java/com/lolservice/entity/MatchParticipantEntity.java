package com.lolservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "match_participants")
@Data
public class MatchParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String matchId;
    private String summonerName;
    private String puuid;
    private String championName;
    private int championId;
    private String teamPosition;
    private boolean win;
    private int kills;
    private int deaths;
    private int assists;
    private int totalDamageDealtToChampions;
    private int visionScore;
    private int goldEarned;
    private int totalMinionsKilled;
    private int neutralMinionsKilled;
    private long createdAt;
    private int gameDuration;
}