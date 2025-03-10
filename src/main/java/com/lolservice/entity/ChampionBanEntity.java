package com.lolservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "champion_bans")
@Data
@NoArgsConstructor
public class ChampionBanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String matchId;

    @Column(nullable = false)
    private String championId;

    private int teamId;
    private int pickTurn;

    public ChampionBanEntity(String matchId, String championId, int teamId, int pickTurn) {
        this.matchId = matchId;
        this.championId = championId;
        this.teamId = teamId;
        this.pickTurn = pickTurn;
    }
} 