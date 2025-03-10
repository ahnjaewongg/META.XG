package com.lolservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "champion_statistics_positions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "championId", "position" })
})
@Data
@NoArgsConstructor
public class ChampionStatisticsPositionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String championId;

    @Column(nullable = false)
    private String position;

    private double pickRate;
    private double winRate;

    public ChampionStatisticsPositionsEntity(String championId, String position, double pickRate, double winRate) {
        this.championId = championId;
        this.position = position;
        this.pickRate = pickRate;
        this.winRate = winRate;
    }
}