package com.lolservice.dto;

import lombok.Data;

@Data
public class SummonerDTO {
    private String id; 
    private String accountId; 
    private String puuid; 
    private int profileIconId; 
    private long revisionDate;
    private long summonerLevel; 
    private String name;
}