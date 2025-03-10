package com.lolservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "champion_statistics")
@Data
@NoArgsConstructor
public class ChampionStatisticsEntity {
    @Id
    private String championId;
    private String championName;
    private String nameKo;
    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "champion_positions_mapping", joinColumns = @JoinColumn(name = "champion_id"))
    @Column(name = "position")
    private List<String> positions;

    private double winRate;
    private double pickRate;
    private double banRate;
    private String tier;
    private long totalGames;
    private long wins;
    private long bans;
    private double score;

    public ChampionStatisticsEntity(String championId, String championName) {
        this.championId = championId;
        this.championName = championName;
        this.totalGames = 0;
        this.wins = 0;
    }

    public void addGame(ChallengerParticipantEntity participant) {
        totalGames++;
        if (participant.isWinner()) {
            wins++;
        }
    }

    public void addBan() {
        bans++;
    }

    public void calculateRates(int totalMatches) {
        if (totalGames > 0) {
            this.winRate = (double) wins / totalGames * 100;
            this.pickRate = (double) totalGames / totalMatches * 100;
        }
        if (bans > 0) {
            this.banRate = (double) bans / totalMatches * 100;
        }
    }
}