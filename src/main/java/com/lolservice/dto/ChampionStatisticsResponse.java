package com.lolservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ChampionStatisticsResponse {
    private String championId;
    private String championName;
    private String nameKo;
    private String imageUrl;
    private List<String> positions;
    private double winRate;
    private double pickRate;
    private double banRate;
    private String tier;
}