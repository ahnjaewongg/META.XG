package com.lolservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 챔피언 포지션 정보를 저장하는 엔티티
 * 각 챔피언별로 포지션별 픽률, 승률, 밴률 정보를 저장합니다.
 */
@Entity
@Table(name = "champion_positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampionPositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "champion_id", nullable = false)
    private Integer championId;

    @Column(name = "champion_name", nullable = true)
    private String championName;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "pick_rate", nullable = false)
    private Double pickRate;

    @Column(name = "win_rate", nullable = false)
    private Double winRate;

    @Column(name = "ban_rate")
    private Double banRate;

    @Column(name = "total_matches")
    private Integer totalMatches;

    @Column(name = "patch_version")
    private String patchVersion;
}