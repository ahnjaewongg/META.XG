package com.lolservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantDTO {
    private String summonerId;
    private String championName;
    private Integer championId;
    private String teamPosition;
    private boolean win;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer totalDamageDealtToChampions;
    private Integer goldEarned;
    private Integer visionScore;
} 