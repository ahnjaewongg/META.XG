package com.lolservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Data;

@Entity
@Table(name = "rankings")
@Data
public class RankingEntity {
    @Id
    private String summonerId;
    private String summonerName;
    private String queueType;
    private String tier;
    
    @Column(name = "rank_value")
    private String rank;
    
    private int leaguePoints;
    private int wins;
    private int losses;
    private boolean veteran;
    private boolean inactive;
    private boolean freshBlood;
    private boolean hotStreak;
} 